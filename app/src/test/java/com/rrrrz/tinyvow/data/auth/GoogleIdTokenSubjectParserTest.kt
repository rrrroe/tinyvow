package com.rrrrz.tinyvow.data.auth

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoogleIdTokenSubjectParserTest {
    @Test
    fun parseSubject_returnsSubjectFromJwtPayload() {
        val payload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("""{"sub":"google-subject-123"}""".toByteArray())
        val token = "header.$payload.signature"

        assertEquals("google-subject-123", GoogleIdTokenSubjectParser.parseSubject(token))
    }

    @Test
    fun parseSubject_returnsNullForInvalidToken() {
        assertNull(GoogleIdTokenSubjectParser.parseSubject("invalid"))
    }
}
