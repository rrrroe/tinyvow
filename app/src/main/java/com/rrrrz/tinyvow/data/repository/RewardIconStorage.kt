package com.rrrrz.tinyvow.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class RewardIconStorage(
    private val rootDir: File,
) {
    fun importImage(
        contentResolver: ContentResolver,
        sourceUri: Uri,
    ): String {
        rootDir.mkdirs()
        val extension = guessExtension(contentResolver, sourceUri)
        val targetFile = File(rootDir, "${UUID.randomUUID()}.$extension")
        contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Unable to open reward icon source." }
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return targetFile.absolutePath
    }

    fun deleteImportedIcon(path: String?) {
        val file = resolveManagedFile(path) ?: return
        if (file.exists()) {
            file.delete()
        }
    }

    fun clearAll() {
        if (rootDir.exists()) {
            rootDir.deleteRecursively()
        }
    }

    fun isManagedImportedPath(path: String?): Boolean = resolveManagedFile(path) != null

    private fun resolveManagedFile(path: String?): File? {
        val normalized = path?.trim().orEmpty()
        if (normalized.isBlank()) return null
        val file = File(normalized)
        val rootPath = rootDir.canonicalFile.toPath()
        val filePath = runCatching { file.canonicalFile.toPath() }.getOrNull() ?: return null
        return if (filePath.startsWith(rootPath)) file else null
    }

    private fun guessExtension(
        contentResolver: ContentResolver,
        sourceUri: Uri,
    ): String {
        val mimeType = contentResolver.getType(sourceUri).orEmpty()
        return when {
            mimeType.endsWith("png") -> "png"
            mimeType.endsWith("webp") -> "webp"
            mimeType.endsWith("gif") -> "gif"
            else -> "jpg"
        }
    }

    companion object {
        fun fromContext(context: Context): RewardIconStorage = RewardIconStorage(File(context.filesDir, DIRECTORY_NAME))

        private const val DIRECTORY_NAME = "reward_icons"
    }
}
