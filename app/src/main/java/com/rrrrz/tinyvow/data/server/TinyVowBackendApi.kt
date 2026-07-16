package com.rrrrz.tinyvow.data.server

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackendSessionResponse(
    val accessToken: String,
    val userId: String,
    val deviceId: String,
    val entitlement: ProEntitlementState,
)

data class BackendPaymentOrder(
    val orderId: String,
    val status: String,
    val displayPrice: String,
)

data class BackendPaymentLaunch(
    val order: BackendPaymentOrder,
    val orderString: String,
)

class TinyVowBackendException(
    val statusCode: Int,
    val errorCode: String,
) : IllegalStateException(errorCode)

interface TinyVowBackendApi {
    suspend fun authenticateAnonymous(installId: String, deviceSecret: String): BackendSessionResponse
    suspend fun getProducts(): List<SubscriptionOffer>
    suspend fun getEntitlement(accessToken: String): ProEntitlementState
    suspend fun redeemActivationCode(accessToken: String, code: String): ProEntitlementState
    suspend fun deleteAccount(accessToken: String)
    suspend fun createPaymentOrder(
        accessToken: String,
        productId: String,
        provider: String,
        clientRequestId: String,
    ): BackendPaymentLaunch
    suspend fun getPaymentOrder(accessToken: String, orderId: String): BackendPaymentOrder
}

class HttpTinyVowBackendApi(
    baseUrl: String,
    private val platform: String = "android",
    private val deviceName: String?,
    private val appVersion: String,
    private val channel: String,
) : TinyVowBackendApi {
    private val baseUrl = baseUrl.trimEnd('/')

    override suspend fun authenticateAnonymous(installId: String, deviceSecret: String): BackendSessionResponse {
        val response = request(
            method = "POST",
            path = "/v1/auth/anonymous",
            body = JSONObject()
                .put("installId", installId)
                .put("deviceSecret", deviceSecret)
                .put("platform", platform)
                .put("deviceName", deviceName)
                .put("appVersion", appVersion)
                .put("channel", channel),
        )
        val root = JSONObject(response)
        return BackendSessionResponse(
            accessToken = root.getString("accessToken"),
            userId = root.getJSONObject("user").getString("userId"),
            deviceId = root.getJSONObject("device").getString("deviceId"),
            entitlement = root.getJSONObject("entitlement").toEntitlementState(),
        )
    }

    override suspend fun getProducts(): List<SubscriptionOffer> {
        val response = request(method = "GET", path = "/v1/products")
        val array = JSONArray(response)
        return buildList {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    SubscriptionOffer(
                        id = item.getString("productId"),
                        productId = item.getString("productId"),
                        offerToken = "",
                        title = item.getString("title"),
                        price = item.getString("displayPrice"),
                        billingPeriod = when (item.getString("planType")) {
                            "MONTHLY" -> "P1M"
                            "YEARLY" -> "P1Y"
                            "LIFETIME" -> "LIFETIME"
                            else -> item.getString("planType")
                        },
                    ),
                )
            }
        }
    }

    override suspend fun getEntitlement(accessToken: String): ProEntitlementState =
        JSONObject(request("GET", "/v1/entitlement", accessToken)).toEntitlementState()

    override suspend fun redeemActivationCode(accessToken: String, code: String): ProEntitlementState {
        val response = request(
            method = "POST",
            path = "/v1/activation-codes/redeem",
            accessToken = accessToken,
            body = JSONObject().put("code", code),
        )
        return JSONObject(response).getJSONObject("entitlement").toEntitlementState()
    }

    override suspend fun deleteAccount(accessToken: String) {
        request(method = "DELETE", path = "/v1/me", accessToken = accessToken)
    }

    override suspend fun createPaymentOrder(
        accessToken: String,
        productId: String,
        provider: String,
        clientRequestId: String,
    ): BackendPaymentLaunch {
        val response = JSONObject(
            request(
                method = "POST",
                path = "/v1/orders",
                accessToken = accessToken,
                body = JSONObject()
                    .put("productId", productId)
                    .put("provider", provider)
                    .put("clientRequestId", clientRequestId),
            ),
        )
        return BackendPaymentLaunch(
            order = response.getJSONObject("order").toPaymentOrder(),
            orderString = response.getJSONObject("launch").getString("orderString"),
        )
    }

    override suspend fun getPaymentOrder(accessToken: String, orderId: String): BackendPaymentOrder =
        JSONObject(request("GET", "/v1/orders/$orderId", accessToken)).toPaymentOrder()

    private suspend fun request(
        method: String,
        path: String,
        accessToken: String? = null,
        body: JSONObject? = null,
    ): String = withContext(Dispatchers.IO) {
        require(baseUrl.startsWith("https://")) { "Tiny Vow backend must use HTTPS." }
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.setRequestProperty("Accept", "application/json")
            accessToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            body?.let { json ->
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.outputStream.use { output ->
                    output.write(json.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val statusCode = connection.responseCode
            val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            if (statusCode !in 200..299) {
                val errorCode = runCatching { JSONObject(responseBody).optString("error") }
                    .getOrNull()
                    .orEmpty()
                    .ifBlank { "http_$statusCode" }
                throw TinyVowBackendException(statusCode, errorCode)
            }
            responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toEntitlementState(): ProEntitlementState {
        val status = optString("status").uppercase()
        val productId = optString("productId").takeIf { it.isNotBlank() && it != "null" }
        val source = optString("source").takeIf { it.isNotBlank() && it != "null" }
        val expiresAtMillis = optString("expiresAt")
            .takeIf { it.isNotBlank() && it != "null" }
            ?.let { Instant.parse(it).toEpochMilli() }
        return when (status) {
            "ACTIVE" -> ProEntitlementState(
                status = ProEntitlementStatus.ACTIVE,
                productId = productId ?: "tinyvow_pro",
                purchaseToken = "backend",
                expiresAtMillis = expiresAtMillis,
                source = source ?: "backend",
            )
            "PENDING" -> ProEntitlementState.pending()
            else -> ProEntitlementState.Free
        }
    }

    private fun JSONObject.toPaymentOrder(): BackendPaymentOrder =
        BackendPaymentOrder(
            orderId = getString("orderId"),
            status = getString("status"),
            displayPrice = getString("displayPrice"),
        )

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 10_000
        const val READ_TIMEOUT_MILLIS = 15_000
    }
}
