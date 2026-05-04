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
        require(parts.size == 3 && parts[0] == ACTIVATION_CODE_PREFIX) { "激活码格式不正确" }

        val payloadPart = parts[1]
        val signatureBytes = decodeUrl(parts[2])
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(payloadPart.toByteArray(StandardCharsets.UTF_8))
        require(signature.verify(signatureBytes)) { "激活码签名无效" }

        val payloadJson = String(decodeUrl(payloadPart), StandardCharsets.UTF_8)
        return ActivationCodePayload.fromJsonString(payloadJson)
    }

    companion object {
        fun encodeUrl(bytes: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        fun decodeUrl(value: String): ByteArray =
            Base64.getUrlDecoder().decode(value)
    }
}
