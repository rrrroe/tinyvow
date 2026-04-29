package com.rrrrz.tinyvow.data.server

data class GoogleSignInExchangeRequest(
    val googleIdToken: String,
    val googleSubject: String,
    val email: String?,
    val displayName: String?,
    val deviceId: String?,
)

data class BackendSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtMillis: Long?,
    val localOnly: Boolean = false,
)

interface AuthApi {
    suspend fun exchangeGoogleSignIn(request: GoogleSignInExchangeRequest): Result<BackendSession>
    suspend fun deleteAccount(accessToken: String): Result<Unit>
}

class NoOpAuthApi : AuthApi {
    override suspend fun exchangeGoogleSignIn(request: GoogleSignInExchangeRequest): Result<BackendSession> =
        Result.success(
            BackendSession(
                userId = "local:${request.googleSubject}",
                accessToken = "",
                refreshToken = null,
                expiresAtMillis = null,
                localOnly = true,
            )
        )

    override suspend fun deleteAccount(accessToken: String): Result<Unit> =
        Result.success(Unit)
}
