package com.rrrrz.tinyvow.data.supermode

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SuperModeCrypto {
    private const val SALT_BYTES = 16
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256

    fun newSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashSecret(secret: String, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return factory.generateSecret(spec).encoded.let(Base64.getEncoder()::encodeToString)
    }

    fun verifySecret(
        secret: String,
        saltBase64: String,
        expectedHashBase64: String,
    ): Boolean {
        val actual = hashSecret(secret, saltBase64)
        return MessageDigest.isEqual(
            Base64.getDecoder().decode(actual),
            Base64.getDecoder().decode(expectedHashBase64),
        )
    }
}
