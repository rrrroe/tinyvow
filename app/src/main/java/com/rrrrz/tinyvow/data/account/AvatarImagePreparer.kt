package com.rrrrz.tinyvow.data.account

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt

internal data class AvatarUploadPayload(
    val bytes: ByteArray,
    val contentType: String,
    val extension: String,
)

/**
 * Normalizes picker results before upload.
 *
 * Photo providers do not always report a MIME type that matches the bytes they return. Preserve
 * already-supported images when possible; otherwise decode and convert the selected visual media
 * to a bounded JPEG that the avatar API can validate consistently.
 */
internal object AvatarImagePreparer {
    const val MAX_UPLOAD_BYTES = 2 * 1024 * 1024
    private const val MAX_SOURCE_BYTES = 32 * 1024 * 1024
    private const val MAX_AVATAR_DIMENSION = 1_024
    private const val MIN_AVATAR_DIMENSION = 256

    fun prepare(contentResolver: ContentResolver, uri: Uri): AvatarUploadPayload {
        val sourceBytes = contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                require(total <= MAX_SOURCE_BYTES) { "avatar_too_large" }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: error("avatar_unavailable")

        require(sourceBytes.isNotEmpty()) { "avatar_content_invalid" }
        supportedImageType(sourceBytes)?.let { imageType ->
            if (sourceBytes.size <= MAX_UPLOAD_BYTES) {
                return AvatarUploadPayload(
                    bytes = sourceBytes,
                    contentType = imageType.contentType,
                    extension = imageType.extension,
                )
            }
        }

        val decoded = decodeScaledBitmap(sourceBytes)
            ?: throw IllegalArgumentException("avatar_content_invalid")
        return try {
            encodeJpegWithinLimit(decoded)
        } finally {
            decoded.recycle()
        }
    }

    internal fun supportedImageType(bytes: ByteArray): SupportedAvatarImageType? =
        when {
            bytes.size >= 3 &&
                unsigned(bytes[0]) == 0xff &&
                unsigned(bytes[1]) == 0xd8 &&
                unsigned(bytes[2]) == 0xff -> SupportedAvatarImageType.JPEG
            bytes.size >= PNG_SIGNATURE.size &&
                bytes.copyOfRange(0, PNG_SIGNATURE.size).contentEquals(PNG_SIGNATURE) ->
                SupportedAvatarImageType.PNG
            bytes.size >= 12 &&
                String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP" ->
                SupportedAvatarImageType.WEBP
            else -> null
        }

    private fun decodeScaledBitmap(bytes: ByteArray): Bitmap? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes))) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    targetSize(info.size.width, info.size.height)?.let { (width, height) ->
                        decoder.setTargetSize(width, height)
                    }
                }
            } else {
                decodeScaledBitmapLegacy(bytes)
            }
        }.getOrNull()

    private fun decodeScaledBitmapLegacy(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (max(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > MAX_AVATAR_DIMENSION * 2) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        ) ?: return null
        val size = targetSize(decoded.width, decoded.height) ?: return decoded
        val scaled = Bitmap.createScaledBitmap(decoded, size.first, size.second, true)
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    private fun encodeJpegWithinLimit(source: Bitmap): AvatarUploadPayload {
        var bitmap = flattenTransparency(source)
        try {
            while (true) {
                for (quality in listOf(92, 85, 75, 65, 55)) {
                    val bytes = ByteArrayOutputStream().use { output ->
                        check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                            "avatar_content_invalid"
                        }
                        output.toByteArray()
                    }
                    if (bytes.size <= MAX_UPLOAD_BYTES) {
                        return AvatarUploadPayload(bytes, "image/jpeg", "jpg")
                    }
                }

                val longestSide = max(bitmap.width, bitmap.height)
                check(longestSide > MIN_AVATAR_DIMENSION) { "avatar_too_large" }
                val scale = max(MIN_AVATAR_DIMENSION.toFloat() / longestSide, 0.75f)
                val next = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                    (bitmap.height * scale).roundToInt().coerceAtLeast(1),
                    true,
                )
                if (bitmap !== source) bitmap.recycle()
                bitmap = next
            }
        } finally {
            if (bitmap !== source) bitmap.recycle()
        }
    }

    private fun flattenTransparency(source: Bitmap): Bitmap {
        if (!source.hasAlpha()) return source
        return Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888).also { target ->
            Canvas(target).apply {
                drawColor(Color.WHITE)
                drawBitmap(source, 0f, 0f, null)
            }
        }
    }

    private fun targetSize(width: Int, height: Int): Pair<Int, Int>? {
        val longestSide = max(width, height)
        if (longestSide <= MAX_AVATAR_DIMENSION) return null
        val scale = MAX_AVATAR_DIMENSION.toFloat() / longestSide
        return (width * scale).roundToInt().coerceAtLeast(1) to
            (height * scale).roundToInt().coerceAtLeast(1)
    }

    private fun unsigned(byte: Byte): Int = byte.toInt() and 0xff

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4e,
        0x47,
        0x0d,
        0x0a,
        0x1a,
        0x0a,
    )
}

internal enum class SupportedAvatarImageType(
    val contentType: String,
    val extension: String,
) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp"),
}
