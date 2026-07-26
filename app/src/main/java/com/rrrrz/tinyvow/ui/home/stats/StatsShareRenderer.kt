package com.rrrrz.tinyvow.ui.home

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.rrrrz.tinyvow.i18n.AppText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal fun shareReportBitmap(
    context: Context,
    bitmap: Bitmap,
) {
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(shareDir, "tinyvow-report-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, AppText.t("stats_tiny_vow_report"))
            clipData = ClipData.newUri(context.contentResolver, AppText.t("stats_tiny_vow_report"), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    val chooser = Intent.createChooser(intent, AppText.t("stats_share_report"))
    if (context !is Activity) {
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

internal fun saveReportBitmap(
    context: Context,
    bitmap: Bitmap,
) {
    val displayName = "tinyvow-report-${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver
    val values =
        android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Tiny Vow",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
    val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    val uri = resolver.insert(collection, values) ?: throw IOException("Unable to create report image")
    try {
        val written =
            resolver.openOutputStream(uri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            } ?: false
        if (!written) throw IOException("Unable to write report image")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(
                uri,
                android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                },
                null,
                null,
            )
        }
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}
