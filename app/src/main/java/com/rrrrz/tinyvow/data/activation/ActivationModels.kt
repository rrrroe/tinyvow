package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID

const val ACTIVATION_CODE_PREFIX = "TVA1"
const val ACTIVATION_CHANNEL_CHINA = "china"
const val ACTIVATION_SOURCE_LOCAL = "local_activation"
const val ACTIVATION_TIME_ROLLBACK_TOLERANCE_MILLIS = 10 * 60 * 1000L

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
        require(version == 1) { "不支持的激活码版本" }
        require(this.userId == userId) { "激活码不属于当前用户" }
        require(productId == TINYVOW_PRO_PRODUCT_ID) { "激活码商品不匹配" }
        require(channel == ACTIVATION_CHANNEL_CHINA) { "激活码渠道不匹配" }
        require(durationDays > 0) { "激活码时长无效" }
        require(nowMillis <= validUntilMillis) { "激活码已过兑换有效期" }
        require(codeId !in usedCodeIds) { "激活码已使用" }
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
