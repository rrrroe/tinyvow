package com.rrrrz.tinyvow.data.special

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import org.json.JSONObject

class WeReadApiException(
    val httpCode: Int? = null,
    val apiCode: Int? = null,
    val upgradeRequired: Boolean = false,
    override val message: String,
) : IllegalStateException(message)

class WeReadApiClient {
    fun fetchReadData(
        apiKey: String,
        mode: String,
        baseTimeSeconds: Long? = null,
    ): String = fetchReadData(
        apiKey = apiKey,
        mode = mode,
        baseTimeSeconds = baseTimeSeconds,
        skillVersion = activeSkillVersion,
        allowUpgradeRetry = true,
    )

    private fun fetchReadData(
        apiKey: String,
        mode: String,
        baseTimeSeconds: Long?,
        skillVersion: String,
        allowUpgradeRetry: Boolean,
    ): String {
        val connection = (URL(GATEWAY_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
        }
        val body =
            JSONObject()
                .put("api_name", "/readdata/detail")
                .put("mode", mode)
                .put("skill_version", skillVersion)
                .apply {
                    if (baseTimeSeconds != null) {
                        put("baseTime", baseTimeSeconds)
                    }
                }
                .toString()
        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
        }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (code !in 200..299) {
            throw WeReadApiException(httpCode = code, message = "HTTP $code${formatErrorDetail(response)}")
        }
        val root = JSONObject(response)
        val upgradeInfo = root.optJSONObject("upgrade_info")
        if (upgradeInfo != null) {
            val upgradedVersion =
                if (allowUpgradeRetry) {
                    resolveUpgradedSkillVersion(upgradeInfo)
                        ?.takeIf { it != skillVersion }
                } else {
                    null
                }
            if (upgradedVersion != null) {
                activeSkillVersion = upgradedVersion
                return fetchReadData(
                    apiKey = apiKey,
                    mode = mode,
                    baseTimeSeconds = baseTimeSeconds,
                    skillVersion = upgradedVersion,
                    allowUpgradeRetry = false,
                )
            }
            throw WeReadApiException(
                upgradeRequired = true,
                message = "WeRead Skill needs upgrade",
            )
        }
        val errcode = root.optInt("errcode", 0)
        if (errcode != 0) {
            throw WeReadApiException(
                apiCode = errcode,
                message = root.optString("errmsg").ifBlank { "WeRead API error $errcode" },
            )
        }
        return response
    }

    private fun formatErrorDetail(response: String): String {
        if (response.isBlank()) return ""
        val parsed =
            runCatching {
                val root = JSONObject(response)
                root.optString("errmsg")
                    .ifBlank { root.optString("message") }
                    .ifBlank { root.optString("error") }
            }.getOrDefault("")
        val detail = parsed.ifBlank { response.take(200) }
        return if (detail.isBlank()) "" else ": $detail"
    }

    private fun resolveUpgradedSkillVersion(upgradeInfo: JSONObject): String? {
        directVersionFrom(upgradeInfo)?.let { return it }
        val upgradeUrl =
            upgradeInfo.optString("upgrade_url")
                .ifBlank { upgradeInfo.optString("upgradeUrl") }
                .ifBlank { DEFAULT_SKILL_ZIP_URL }
        return runCatching { fetchSkillVersionFromZip(upgradeUrl) }.getOrNull()
    }

    private fun directVersionFrom(upgradeInfo: JSONObject): String? {
        val keys = listOf("skill_version", "version", "latest_version", "latestVersion", "new_version")
        return keys.firstNotNullOfOrNull { key ->
            upgradeInfo.optString(key).takeIf(::isSkillVersion)
        }
    }

    private fun fetchSkillVersionFromZip(upgradeUrl: String): String? {
        val connection = (URL(upgradeUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            ZipInputStream(connection.inputStream.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith("SKILL.md")) {
                        val content = zip.readBytes().toString(Charsets.UTF_8)
                        return parseSkillVersion(content)
                    }
                    entry = zip.nextEntry
                }
            }
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSkillVersion(skillMarkdown: String): String? =
        SKILL_VERSION_REGEX.find(skillMarkdown)?.groupValues?.getOrNull(1)?.takeIf(::isSkillVersion)

    private fun isSkillVersion(value: String): Boolean = SKILL_VERSION_VALUE_REGEX.matches(value)

    companion object {
        private const val GATEWAY_URL = "https://i.weread.qq.com/api/agent/gateway"
        private const val DEFAULT_SKILL_ZIP_URL = "https://cdn.weread.qq.com/skills/weread-skills.zip"
        private val SKILL_VERSION_REGEX = Regex("""(?m)^\s*version:\s*["']?([0-9]+(?:\.[0-9]+){1,3})["']?\s*$""")
        private val SKILL_VERSION_VALUE_REGEX = Regex("""[0-9]+(?:\.[0-9]+){1,3}""")

        @Volatile
        private var activeSkillVersion = WEREAD_SKILL_VERSION
    }
}
