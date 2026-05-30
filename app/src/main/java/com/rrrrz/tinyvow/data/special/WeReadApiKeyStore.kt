package com.rrrrz.tinyvow.data.special

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeReadApiKeyStore(
    private val context: Context,
    private val preferences: ManagedAppPreferences = ManagedAppPreferences(context),
) {
    suspend fun save(apiKey: String?) {
        withContext(Dispatchers.IO) {
            val normalized = normalize(apiKey)
            preferences.setEncryptedWeReadApiKey(
                if (normalized.isBlank()) null else encrypt(normalized),
            )
        }
    }

    suspend fun get(): String? =
        withContext(Dispatchers.IO) {
            consumePendingRestoredKey()
            preferences.getEncryptedWeReadApiKeyOnce()?.let(::decrypt)?.let(::normalize)
        }

    suspend fun hasKey(): Boolean = get()?.isNotBlank() == true

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) +
            ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String? =
        runCatching {
            val parts = value.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private suspend fun consumePendingRestoredKey() {
        val pendingFile = pendingRestoreFile(context)
        if (!pendingFile.isFile) return
        val restoredKey = normalize(pendingFile.readText(Charsets.UTF_8))
        if (restoredKey.isNotBlank()) {
            preferences.setEncryptedWeReadApiKey(encrypt(restoredKey))
        }
        pendingFile.delete()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "tinyvow_weread_api_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PENDING_RESTORE_FILE_NAME = "weread_api_key_pending_restore"

        fun normalize(apiKey: String?): String =
            apiKey
                ?.trim()
                ?.removeSurrounding("\"")
                ?.removeSurrounding("'")
                ?.replace(Regex("^Bearer\\s+", RegexOption.IGNORE_CASE), "")
                ?.trim()
                .orEmpty()

        fun deleteStoredKeyMaterial() {
            runCatching {
                KeyStore.getInstance(ANDROID_KEYSTORE)
                    .apply { load(null) }
                    .deleteEntry(KEY_ALIAS)
            }
        }

        fun stageRestoredKey(context: Context, apiKey: String) {
            val normalized = normalize(apiKey)
            if (normalized.isBlank()) return
            pendingRestoreFile(context).writeText(normalized, Charsets.UTF_8)
        }

        fun deletePendingRestoredKey(context: Context) {
            pendingRestoreFile(context).delete()
        }

        private fun pendingRestoreFile(context: Context): File =
            File(context.filesDir, PENDING_RESTORE_FILE_NAME)
    }
}
