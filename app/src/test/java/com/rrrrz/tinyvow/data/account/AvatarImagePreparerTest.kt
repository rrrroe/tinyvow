package com.rrrrz.tinyvow.data.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AvatarImagePreparerTest {
    @Test
    fun detectsJpegFromBytesInsteadOfPickerMimeType() {
        val bytes = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xe0.toByte())

        assertEquals(
            SupportedAvatarImageType.JPEG,
            AvatarImagePreparer.supportedImageType(bytes),
        )
    }

    @Test
    fun detectsPngSignature() {
        val bytes = byteArrayOf(
            0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        )

        assertEquals(
            SupportedAvatarImageType.PNG,
            AvatarImagePreparer.supportedImageType(bytes),
        )
    }

    @Test
    fun detectsWebpContainer() {
        val bytes = "RIFF0000WEBP".toByteArray(Charsets.US_ASCII)

        assertEquals(
            SupportedAvatarImageType.WEBP,
            AvatarImagePreparer.supportedImageType(bytes),
        )
    }

    @Test
    fun rejectsUnknownContent() {
        assertNull(AvatarImagePreparer.supportedImageType("not an image".toByteArray()))
    }
}
