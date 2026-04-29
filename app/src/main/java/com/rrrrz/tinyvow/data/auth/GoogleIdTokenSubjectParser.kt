package com.rrrrz.tinyvow.data.auth

import java.util.Base64

object GoogleIdTokenSubjectParser {
    private val subjectRegex = Regex(""""sub"\s*:\s*"([^"]+)"""")

    fun parseSubject(idToken: String): String? {
        val payload = idToken.split('.').getOrNull(1) ?: return null
        return runCatching {
            val normalizedPayload = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
            val json = String(Base64.getUrlDecoder().decode(normalizedPayload), Charsets.UTF_8)
            subjectRegex.find(json)?.groupValues?.getOrNull(1)
        }.getOrNull()
    }
}
