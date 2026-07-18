package com.rrrrz.tinyvow.data.server

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.data.account.BackendAccount
import com.rrrrz.tinyvow.data.account.BackendLoginEvent
import java.io.ByteArrayOutputStream
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
    val account: BackendAccount? = null,
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
    suspend fun getAccount(accessToken: String): BackendAccount
    suspend fun registerAccount(
        accessToken: String,
        email: String,
        password: String,
        displayName: String,
    ): BackendSessionResponse
    suspend fun loginAccount(
        accessToken: String,
        email: String,
        password: String,
    ): BackendSessionResponse
    suspend fun signOutAccount(accessToken: String): BackendSessionResponse
    suspend fun updateAccountProfile(accessToken: String, displayName: String): BackendAccount
    suspend fun requestEmailVerification(accessToken: String)
    suspend fun confirmEmailVerification(accessToken: String, code: String): BackendAccount
    suspend fun requestPasswordReset(email: String)
    suspend fun confirmPasswordReset(email: String, code: String, newPassword: String)
    suspend fun uploadAccountAvatar(
        accessToken: String,
        bytes: ByteArray,
        contentType: String,
        fileName: String,
    ): String?
    suspend fun deleteAccountAvatar(accessToken: String): String?
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
        return JSONObject(response).toBackendSessionResponse()
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

    override suspend fun getAccount(accessToken: String): BackendAccount =
        JSONObject(request("GET", "/v1/account", accessToken)).toBackendAccount()

    override suspend fun registerAccount(
        accessToken: String,
        email: String,
        password: String,
        displayName: String,
    ): BackendSessionResponse =
        JSONObject(
            request(
                method = "POST",
                path = "/v1/account/register",
                accessToken = accessToken,
                body = JSONObject()
                    .put("email", email)
                    .put("password", password)
                    .put("displayName", displayName),
            ),
        ).toBackendSessionResponse()

    override suspend fun loginAccount(
        accessToken: String,
        email: String,
        password: String,
    ): BackendSessionResponse =
        JSONObject(
            request(
                method = "POST",
                path = "/v1/account/login",
                accessToken = accessToken,
                body = JSONObject()
                    .put("email", email)
                    .put("password", password),
            ),
        ).toBackendSessionResponse()

    override suspend fun signOutAccount(accessToken: String): BackendSessionResponse =
        JSONObject(request("POST", "/v1/account/sign-out", accessToken)).toBackendSessionResponse()

    override suspend fun updateAccountProfile(accessToken: String, displayName: String): BackendAccount =
        JSONObject(
            request(
                method = "PATCH",
                path = "/v1/account/profile",
                accessToken = accessToken,
                body = JSONObject().put("displayName", displayName),
            ),
        ).toBackendAccount()

    override suspend fun requestEmailVerification(accessToken: String) {
        request(
            method = "POST",
            path = "/v1/account/email-verification/request",
            accessToken = accessToken,
        )
    }

    override suspend fun confirmEmailVerification(accessToken: String, code: String): BackendAccount =
        JSONObject(
            request(
                method = "POST",
                path = "/v1/account/email-verification/confirm",
                accessToken = accessToken,
                body = JSONObject().put("code", code),
            ),
        ).toBackendAccount()

    override suspend fun requestPasswordReset(email: String) {
        request(
            method = "POST",
            path = "/v1/auth/password-reset/request",
            body = JSONObject().put("email", email),
        )
    }

    override suspend fun confirmPasswordReset(
        email: String,
        code: String,
        newPassword: String,
    ) {
        request(
            method = "POST",
            path = "/v1/auth/password-reset/confirm",
            body = JSONObject()
                .put("email", email)
                .put("code", code)
                .put("newPassword", newPassword),
        )
    }

    override suspend fun uploadAccountAvatar(
        accessToken: String,
        bytes: ByteArray,
        contentType: String,
        fileName: String,
    ): String? {
        val boundary = "TinyVow-${System.currentTimeMillis()}"
        val prefix = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"file\"; filename=\"")
            append(fileName.replace("\"", ""))
            append("\"\r\n")
            append("Content-Type: $contentType\r\n\r\n")
        }.toByteArray(Charsets.UTF_8)
        val suffix = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
        val body = ByteArrayOutputStream(prefix.size + bytes.size + suffix.size).use { output ->
            output.write(prefix)
            output.write(bytes)
            output.write(suffix)
            output.toByteArray()
        }
        return JSONObject(
            requestBytes(
                method = "POST",
                path = "/v1/account/avatar",
                accessToken = accessToken,
                contentType = "multipart/form-data; boundary=$boundary",
                body = body,
            ),
        ).nullableString("avatarUrl")
    }

    override suspend fun deleteAccountAvatar(accessToken: String): String? =
        JSONObject(request("DELETE", "/v1/account/avatar", accessToken))
            .nullableString("avatarUrl")

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
        requestConnection(
            method = method,
            path = path,
            accessToken = accessToken,
            contentType = body?.let { "application/json; charset=utf-8" },
            body = body?.toString()?.toByteArray(Charsets.UTF_8),
        )
    }

    private suspend fun requestBytes(
        method: String,
        path: String,
        accessToken: String?,
        contentType: String,
        body: ByteArray,
    ): String = withContext(Dispatchers.IO) {
        requestConnection(method, path, accessToken, contentType, body)
    }

    private fun requestConnection(
        method: String,
        path: String,
        accessToken: String?,
        contentType: String?,
        body: ByteArray?,
    ): String {
        require(baseUrl.startsWith("https://")) { "Tiny Vow backend must use HTTPS." }
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.setRequestProperty("Accept", "application/json")
            accessToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            body?.let { payload ->
                connection.doOutput = true
                contentType?.let { connection.setRequestProperty("Content-Type", it) }
                connection.outputStream.use { output ->
                    output.write(payload)
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

    private fun JSONObject.toBackendSessionResponse(): BackendSessionResponse =
        BackendSessionResponse(
            accessToken = getString("accessToken"),
            userId = getJSONObject("user").getString("userId"),
            deviceId = getJSONObject("device").getString("deviceId"),
            entitlement = getJSONObject("entitlement").toEntitlementState(),
            account = optJSONObject("account")?.toBackendAccount(),
        )

    private fun JSONObject.toBackendAccount(): BackendAccount {
        val loginArray = optJSONArray("recentLogins") ?: JSONArray()
        return BackendAccount(
            userId = getString("userId"),
            accountType = optString("accountType", BackendAccount.ACCOUNT_TYPE_ANONYMOUS),
            email = nullableString("email"),
            emailVerified = optBoolean("emailVerified", false),
            displayName = nullableString("displayName"),
            avatarUrl = nullableString("avatarUrl"),
            createdAtMillis = instantMillis("createdAt") ?: System.currentTimeMillis(),
            registeredAtMillis = instantMillis("registeredAt"),
            lastLoginAtMillis = instantMillis("lastLoginAt"),
            totalSpentCents = optLong("totalSpentCents", 0L),
            paidOrderCount = optInt("paidOrderCount", 0),
            recentLogins = buildList {
                repeat(loginArray.length()) { index ->
                    val item = loginArray.getJSONObject(index)
                    add(
                        BackendLoginEvent(
                            authMethod = item.optString("authMethod"),
                            platform = item.nullableString("platform"),
                            deviceName = item.nullableString("deviceName"),
                            loggedInAtMillis = item.instantMillis("loggedInAt") ?: 0L,
                        ),
                    )
                }
            },
            entitlement = optJSONObject("entitlement")?.toEntitlementState() ?: ProEntitlementState.Free,
        )
    }

    private fun JSONObject.nullableString(key: String): String? =
        optString(key).takeIf { it.isNotBlank() && it != "null" }

    private fun JSONObject.instantMillis(key: String): Long? =
        nullableString(key)?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }

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
