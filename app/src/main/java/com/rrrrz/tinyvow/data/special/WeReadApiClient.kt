package com.rrrrz.tinyvow.data.special

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class WeReadApiException(
    val httpCode: Int? = null,
    val apiCode: Int? = null,
    override val message: String,
) : IllegalStateException(message)

class WeReadApiClient {
    fun fetchReadData(
        apiKey: String,
        mode: String,
        baseTimeSeconds: Long? = null,
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
                .put("skill_version", WEREAD_SKILL_VERSION)
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
        val errcode = root.optInt("errcode", 0)
        if (errcode != 0) {
            throw WeReadApiException(
                apiCode = errcode,
                message = root.optString("errmsg").ifBlank { "WeRead API error $errcode" },
            )
        }
        if (root.has("upgrade_info")) {
            throw WeReadApiException(
                message = root.optJSONObject("upgrade_info")?.optString("message").orEmpty().ifBlank { "WeRead Skill needs upgrade" },
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

    companion object {
        private const val GATEWAY_URL = "https://i.weread.qq.com/api/agent/gateway"
    }
}
