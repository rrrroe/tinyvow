package com.rrrrz.tinyvow.data.activation

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class ActivationCodeVerifier(
    publicKeyBase64: String,
) {
    private val publicKey = KeyFactory.getInstance("RSA")
        .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)))

    fun verify(code: String): ActivationCodePayload {
        val parts = code.trim().split('.')
        requireActivation(parts.size == 3 && parts[0] == ACTIVATION_CODE_PREFIX, "activation_error_invalid_format")

        val payloadPart = parts[1]
        val signatureBytes =
            runCatching { decodeUrl(parts[2]) }
                .getOrElse { throw ActivationCodeException("activation_error_invalid_format") }
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(payloadPart.toByteArray(StandardCharsets.UTF_8))
        requireActivation(signature.verify(signatureBytes), "activation_error_invalid_signature")

        return runCatching {
            val payloadJson = String(decodeUrl(payloadPart), StandardCharsets.UTF_8)
            ActivationCodePayload.fromJsonString(payloadJson)
        }.getOrElse {
            throw ActivationCodeException("activation_code_invalid")
        }
    }

    companion object {
        fun encodeUrl(bytes: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        fun decodeUrl(value: String): ByteArray =
            Base64.getUrlDecoder().decode(value)
    }
}
