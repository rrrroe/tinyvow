package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID
import com.rrrrz.tinyvow.i18n.AppText

const val ACTIVATION_CODE_PREFIX = "TVA1"
const val ACTIVATION_CHANNEL_CHINA = "china"
const val ACTIVATION_SOURCE_LOCAL = "local_activation"
const val ACTIVATION_TIME_ROLLBACK_TOLERANCE_MILLIS = 10 * 60 * 1000L

class ActivationCodeException(
    val messageKey: String,
) : IllegalArgumentException(AppText.t(messageKey))

data class ActivationCodePayload(
    val version: Int,
    val codeId: String,
    val userId: String,
    val productId: String,
    val channel: String,
    val durationDays: Int,
    val issuedAtMillis: Long,
    val validUntilMillis: Long,
) {
    fun validateFor(userId: String, nowMillis: Long, usedCodeIds: Set<String>) {
        requireActivation(version == 1, "activation_error_unsupported_version")
        requireActivation(this.userId == userId, "activation_error_wrong_user")
        requireActivation(productId == TINYVOW_PRO_PRODUCT_ID, "activation_error_product_mismatch")
        requireActivation(channel == ACTIVATION_CHANNEL_CHINA, "activation_error_channel_mismatch")
        requireActivation(durationDays > 0, "activation_error_invalid_duration")
        requireActivation(nowMillis <= validUntilMillis, "activation_error_expired_code")
        requireActivation(codeId !in usedCodeIds, "activation_error_code_already_used")
    }

    fun toJsonString(): String =
        "{" +
            "\"version\":$version," +
            "\"codeId\":\"${escapeJson(codeId)}\"," +
            "\"userId\":\"${escapeJson(userId)}\"," +
            "\"productId\":\"${escapeJson(productId)}\"," +
            "\"channel\":\"${escapeJson(channel)}\"," +
            "\"durationDays\":$durationDays," +
            "\"issuedAtMillis\":$issuedAtMillis," +
            "\"validUntilMillis\":$validUntilMillis" +
            "}"

    companion object {
        fun fromJsonString(value: String): ActivationCodePayload {
            return ActivationCodePayload(
                version = readJsonInt(value, "version"),
                codeId = readJsonString(value, "codeId"),
                userId = readJsonString(value, "userId"),
                productId = readJsonString(value, "productId"),
                channel = readJsonString(value, "channel"),
                durationDays = readJsonInt(value, "durationDays"),
                issuedAtMillis = readJsonLong(value, "issuedAtMillis"),
                validUntilMillis = readJsonLong(value, "validUntilMillis"),
            )
        }
    }
}

data class LocalActivationRecord(
    val userId: String,
    val codeId: String,
    val productId: String,
    val channel: String,
    val durationDays: Int,
    val activatedAtMillis: Long,
    val expiresAtMillis: Long,
) {
    fun toJsonString(): String =
        "{" +
            "\"userId\":\"${escapeJson(userId)}\"," +
            "\"codeId\":\"${escapeJson(codeId)}\"," +
            "\"productId\":\"${escapeJson(productId)}\"," +
            "\"channel\":\"${escapeJson(channel)}\"," +
            "\"durationDays\":$durationDays," +
            "\"activatedAtMillis\":$activatedAtMillis," +
            "\"expiresAtMillis\":$expiresAtMillis" +
            "}"

    companion object {
        fun fromJsonString(value: String?): LocalActivationRecord? {
            if (value.isNullOrBlank()) return null
            return runCatching {
                LocalActivationRecord(
                    userId = readJsonString(value, "userId"),
                    codeId = readJsonString(value, "codeId"),
                    productId = readJsonString(value, "productId"),
                    channel = readJsonString(value, "channel"),
                    durationDays = readJsonInt(value, "durationDays"),
                    activatedAtMillis = readJsonLong(value, "activatedAtMillis"),
                    expiresAtMillis = readJsonLong(value, "expiresAtMillis"),
                )
            }.getOrNull()
        }
    }
}

data class LegacyActivationClaimSnapshot(
    val record: LocalActivationRecord,
    val usedCodeIds: Set<String>,
)

internal fun requireActivation(condition: Boolean, messageKey: String) {
    if (!condition) throw ActivationCodeException(messageKey)
}

private fun escapeJson(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

private fun readJsonString(json: String, key: String): String {
    val pattern = Regex(""""${Regex.escape(key)}"\s*:\s*"((?:\\.|[^"\\])*)"""")
    val value = pattern.find(json)?.groupValues?.get(1)
        ?: throw IllegalArgumentException("Missing JSON string field: $key")
    return value.replace("\\\"", "\"").replace("\\\\", "\\")
}

private fun readJsonInt(json: String, key: String): Int =
    readJsonLong(json, key).toInt()

private fun readJsonLong(json: String, key: String): Long {
    val pattern = Regex(""""${Regex.escape(key)}"\s*:\s*(-?\d+)""")
    return pattern.find(json)?.groupValues?.get(1)?.toLong()
        ?: throw IllegalArgumentException("Missing JSON number field: $key")
}
