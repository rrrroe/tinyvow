package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ActivationCodeVerifierTest {
    private val keyPair = generateKeyPair()
    private val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)

    @Test
    fun verifiesValidSignedCode() {
        val payload = payload()
        val code = issueCode(payload)

        val verified = ActivationCodeVerifier(publicKeyBase64).verify(code)
        verified.validateFor(
            userId = "user-1",
            nowMillis = 1_700_000_000_000L,
            usedCodeIds = emptySet(),
        )

        assertEquals("code-1", verified.codeId)
        assertEquals(30, verified.durationDays)
    }

    @Test
    fun rejectsTamperedPayload() {
        val code = issueCode(payload())
        val parts = code.split('.').toMutableList()
        parts[1] = ActivationCodeVerifier.encodeUrl(payload(codeId = "code-2").toJsonString().toByteArray())

        assertThrows(IllegalArgumentException::class.java) {
            ActivationCodeVerifier(publicKeyBase64).verify(parts.joinToString("."))
        }
    }

    @Test
    fun rejectsMismatchedUser() {
        val verified = ActivationCodeVerifier(publicKeyBase64).verify(issueCode(payload()))

        assertThrows(IllegalArgumentException::class.java) {
            verified.validateFor(
                userId = "other-user",
                nowMillis = 1_700_000_000_000L,
                usedCodeIds = emptySet(),
            )
        }
    }

    @Test
    fun rejectsExpiredActivationCode() {
        val verified = ActivationCodeVerifier(publicKeyBase64).verify(issueCode(payload()))

        assertThrows(IllegalArgumentException::class.java) {
            verified.validateFor(
                userId = "user-1",
                nowMillis = 1_700_100_000_000L,
                usedCodeIds = emptySet(),
            )
        }
    }

    @Test
    fun rejectsRepeatedCodeId() {
        val verified = ActivationCodeVerifier(publicKeyBase64).verify(issueCode(payload()))

        assertThrows(IllegalArgumentException::class.java) {
            verified.validateFor(
                userId = "user-1",
                nowMillis = 1_700_000_000_000L,
                usedCodeIds = setOf("code-1"),
            )
        }
    }

    private fun payload(codeId: String = "code-1") = ActivationCodePayload(
        version = 1,
        codeId = codeId,
        userId = "user-1",
        productId = TINYVOW_PRO_PRODUCT_ID,
        channel = ACTIVATION_CHANNEL_CHINA,
        durationDays = 30,
        issuedAtMillis = 1_699_999_000_000L,
        validUntilMillis = 1_700_010_000_000L,
    )

    private fun issueCode(payload: ActivationCodePayload): String {
        val payloadPart = ActivationCodeVerifier.encodeUrl(payload.toJsonString().toByteArray(StandardCharsets.UTF_8))
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(payloadPart.toByteArray(StandardCharsets.UTF_8))
        val signaturePart = ActivationCodeVerifier.encodeUrl(signer.sign())
        return "$ACTIVATION_CODE_PREFIX.$payloadPart.$signaturePart"
    }

    private fun generateKeyPair(): KeyPair =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
}
