package com.rrrrz.tinyvow.data.privacy

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalBackupImportValidationTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun validateBackupManifestFields_acceptsCurrentPackageAndVersion() {
        validateBackupManifestFields(
            format = LocalDataManager.BACKUP_FORMAT,
            schemaVersion = LocalDataManager.BACKUP_SCHEMA_VERSION,
            packageName = "com.rrrrz.tinyvow.cn",
            appVersionCode = 10,
            expectedPackageName = "com.rrrrz.tinyvow.cn",
            currentVersionCode = 10,
        )
    }

    @Test
    fun validateBackupManifestFields_rejectsCrossPackageBackup() {
        assertIllegalArgument {
            validateBackupManifestFields(
                format = LocalDataManager.BACKUP_FORMAT,
                schemaVersion = LocalDataManager.BACKUP_SCHEMA_VERSION,
                packageName = "com.rrrrz.tinyvow",
                appVersionCode = 10,
                expectedPackageName = "com.rrrrz.tinyvow.cn",
                currentVersionCode = 10,
            )
        }
    }

    @Test
    fun validateBackupManifestFields_rejectsNewerBackupVersion() {
        assertIllegalArgument {
            validateBackupManifestFields(
                format = LocalDataManager.BACKUP_FORMAT,
                schemaVersion = LocalDataManager.BACKUP_SCHEMA_VERSION,
                packageName = "com.rrrrz.tinyvow.cn",
                appVersionCode = 11,
                expectedPackageName = "com.rrrrz.tinyvow.cn",
                currentVersionCode = 10,
            )
        }
    }

    @Test
    fun copyWithLimit_rejectsOversizedBackupFile() {
        assertIllegalArgument {
            copyWithLimit(
                input = ByteArrayInputStream(ByteArray(4)),
                output = ByteArrayOutputStream(),
                maxBytes = 3,
            )
        }
    }

    @Test
    fun extractBackupZipWithLimits_rejectsTooManyEntries() {
        val zip = writeZip(
            listOf(
                "one.txt" to byteArrayOf(1),
                "two.txt" to byteArrayOf(2),
                "three.txt" to byteArrayOf(3),
            ),
        )

        assertIllegalArgument {
            extractBackupZipWithLimits(
                zipFile = zip,
                destination = temp.newFolder("entries"),
                limits = BackupImportLimits(maxUnzippedBytes = 128, maxEntryBytes = 64, maxEntries = 2),
            )
        }
    }

    @Test
    fun extractBackupZipWithLimits_rejectsOversizedEntry() {
        val zip = writeZip(listOf("large.bin" to ByteArray(8)))

        assertIllegalArgument {
            extractBackupZipWithLimits(
                zipFile = zip,
                destination = temp.newFolder("entry"),
                limits = BackupImportLimits(maxUnzippedBytes = 128, maxEntryBytes = 7, maxEntries = 10),
            )
        }
    }

    @Test
    fun extractBackupZipWithLimits_rejectsOversizedUnzippedTotal() {
        val zip = writeZip(
            listOf(
                "one.bin" to ByteArray(4),
                "two.bin" to ByteArray(4),
            ),
        )

        assertIllegalArgument {
            extractBackupZipWithLimits(
                zipFile = zip,
                destination = temp.newFolder("total"),
                limits = BackupImportLimits(maxUnzippedBytes = 7, maxEntryBytes = 64, maxEntries = 10),
            )
        }
    }

    @Test
    fun extractBackupZipWithLimits_rejectsPathTraversal() {
        val zip = writeZip(listOf("../escape.txt" to byteArrayOf(1)))
        val destination = temp.newFolder("traversal")

        assertIllegalArgument {
            extractBackupZipWithLimits(
                zipFile = zip,
                destination = destination,
                limits = BackupImportLimits(maxUnzippedBytes = 128, maxEntryBytes = 64, maxEntries = 10),
            )
        }
        assertTrue(File(destination.parentFile, "escape.txt").exists().not())
    }

    private fun writeZip(entries: List<Pair<String, ByteArray>>): File {
        val zip = temp.newFile("backup-${System.nanoTime()}.zip")
        ZipOutputStream(FileOutputStream(zip)).use { output ->
            entries.forEach { (name, bytes) ->
                output.putNextEntry(ZipEntry(name))
                output.write(bytes)
                output.closeEntry()
            }
        }
        return zip
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}
