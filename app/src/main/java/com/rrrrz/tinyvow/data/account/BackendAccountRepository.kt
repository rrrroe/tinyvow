package com.rrrrz.tinyvow.data.account

import android.content.ContentResolver
import android.net.Uri
import com.rrrrz.tinyvow.data.server.BackendSessionResponse
import com.rrrrz.tinyvow.data.server.BackendSubscriptionStore
import com.rrrrz.tinyvow.data.server.TinyVowBackendApi
import com.rrrrz.tinyvow.data.server.TinyVowBackendException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BackendAccountRepository(
    private val api: TinyVowBackendApi,
    private val store: BackendSubscriptionStore,
) {
    private val mutex = Mutex()
    private var installId: String? = null

    private val _account = MutableStateFlow<BackendAccount?>(null)
    val account: StateFlow<BackendAccount?> = _account.asStateFlow()

    suspend fun initialize(localInstallId: String): Result<Unit> = mutex.withLock {
        runCatching {
            installId = localInstallId
            val stored = store.load()
            _account.value = stored?.account
            if (stored == null || stored.installId != localInstallId) {
                saveSession(authenticate(localInstallId))
            } else {
                refreshAccount(stored.accessToken, localInstallId)
            }
        }
    }

    suspend fun refresh(): Result<Unit> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            refreshAccount(session.accessToken, currentInstallId)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): Result<BackendAccount> =
        mutex.withLock {
            runCatching {
                val currentInstallId = requireInstallId()
                val session = ensureSession(currentInstallId)
                val response = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                    api.registerAccount(accessToken, email.trim(), password, displayName.trim())
                }
                saveSession(response)
                requireNotNull(response.account)
            }
        }

    suspend fun login(email: String, password: String): Result<BackendAccount> =
        mutex.withLock {
            runCatching {
                val currentInstallId = requireInstallId()
                val session = ensureSession(currentInstallId)
                val response = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                    api.loginAccount(accessToken, email.trim(), password)
                }
                saveSession(response)
                requireNotNull(response.account)
            }
        }

    suspend fun signOut(): Result<Unit> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            val response = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                api.signOutAccount(accessToken)
            }
            saveSession(response)
        }
    }

    suspend fun updateDisplayName(displayName: String): Result<BackendAccount> =
        mutex.withLock {
            runCatching {
                val currentInstallId = requireInstallId()
                val session = ensureSession(currentInstallId)
                val account = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                    api.updateAccountProfile(accessToken, displayName.trim())
                }
                saveAccount(account)
                account
            }
        }

    suspend fun requestEmailVerification(): Result<Unit> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                api.requestEmailVerification(accessToken)
            }
        }
    }

    suspend fun confirmEmailVerification(code: String): Result<BackendAccount> =
        mutex.withLock {
            runCatching {
                val currentInstallId = requireInstallId()
                val session = ensureSession(currentInstallId)
                val account = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                    api.confirmEmailVerification(accessToken, code.trim())
                }
                saveAccount(account)
                account
            }
        }

    suspend fun requestPasswordReset(email: String): Result<Unit> = mutex.withLock {
        runCatching {
            api.requestPasswordReset(email.trim())
        }
    }

    suspend fun confirmPasswordReset(
        email: String,
        code: String,
        newPassword: String,
    ): Result<Unit> = mutex.withLock {
        runCatching {
            api.confirmPasswordReset(
                email = email.trim(),
                code = code.trim(),
                newPassword = newPassword,
            )
        }
    }

    suspend fun uploadAvatar(
        contentResolver: ContentResolver,
        uri: Uri,
    ): Result<BackendAccount> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            val avatar = AvatarImagePreparer.prepare(contentResolver, uri)
            val avatarUrl = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                api.uploadAccountAvatar(
                    accessToken = accessToken,
                    bytes = avatar.bytes,
                    contentType = avatar.contentType,
                    fileName = "avatar.${avatar.extension}",
                )
            }
            val next = requireNotNull(_account.value).copy(avatarUrl = avatarUrl)
            saveAccount(next)
            next
        }
    }

    suspend fun deleteAvatar(): Result<BackendAccount> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            val avatarUrl = retryUnauthorized(currentInstallId, session.accessToken) { accessToken ->
                api.deleteAccountAvatar(accessToken)
            }
            val next = requireNotNull(_account.value).copy(avatarUrl = avatarUrl)
            saveAccount(next)
            next
        }
    }

    suspend fun deleteAccount(): Result<Unit> = mutex.withLock {
        runCatching {
            val currentInstallId = requireInstallId()
            val session = ensureSession(currentInstallId)
            try {
                api.deleteAccount(session.accessToken)
            } catch (error: TinyVowBackendException) {
                if (error.statusCode != 401) throw error
            }
            store.clear()
            _account.value = null
        }
    }

    private suspend fun refreshAccount(accessToken: String, localInstallId: String) {
        val account = retryUnauthorized(localInstallId, accessToken) { token ->
            api.getAccount(token)
        }
        saveAccount(account)
    }

    private suspend fun ensureSession(localInstallId: String) =
        store.load()?.takeIf { it.installId == localInstallId }
            ?: authenticate(localInstallId).also { saveSession(it) }.let { requireNotNull(store.load()) }

    private suspend fun authenticate(localInstallId: String): BackendSessionResponse =
        api.authenticateAnonymous(
            installId = localInstallId,
            deviceSecret = store.getOrCreateDeviceSecret(),
        )

    private suspend fun saveSession(response: BackendSessionResponse) {
        val currentInstallId = requireInstallId()
        store.saveSession(currentInstallId, response)
        response.account?.let { _account.value = it }
    }

    private suspend fun saveAccount(account: BackendAccount) {
        store.saveAccount(account)
        _account.value = account
    }

    private suspend fun <T> retryUnauthorized(
        localInstallId: String,
        accessToken: String,
        action: suspend (String) -> T,
    ): T =
        try {
            action(accessToken)
        } catch (error: TinyVowBackendException) {
            if (error.statusCode != 401) throw error
            val renewed = authenticate(localInstallId)
            saveSession(renewed)
            action(renewed.accessToken)
        }

    private fun requireInstallId(): String =
        installId?.takeIf { it.isNotBlank() } ?: error("account_repository_not_initialized")

}
