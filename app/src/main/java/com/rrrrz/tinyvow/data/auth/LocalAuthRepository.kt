package com.rrrrz.tinyvow.data.auth

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.i18n.AppText
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

class LocalAuthRepository(
    private val context: Context,
) : AuthRepository {
    private val credentialManager = CredentialManager.create(context)

    private object Keys {
        val sessionJson = stringPreferencesKey("session_json")
    }

    override val isGoogleSignInConfigured: Boolean =
        BuildConfig.ENABLE_GOOGLE_LOGIN && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    override val session: Flow<UserSession?> =
        context.authDataStore.data.map { preferences ->
            decodeSession(preferences[Keys.sessionJson])
        }

    override suspend fun ensureLocalSession(): UserSession {
        val existing = session.first()
        if (existing != null) return existing

        val now = System.currentTimeMillis()
        val userId = UUID.randomUUID().toString()
        val nextSession = UserSession(
            userId = userId,
            provider = "local_china",
            providerSubject = userId,
            email = null,
            displayName = null,
            avatarUrl = null,
            createdAt = now,
            lastSignedInAt = now,
        )
        saveSession(nextSession)
        return nextSession
    }

    override suspend fun signInWithGoogle(activity: ComponentActivity): Result<UserSession> {
        if (!isGoogleSignInConfigured) {
            return Result.failure(IllegalStateException(AppText.t("auth_error_google_web_client_missing")))
        }

        return runCatching {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                context = activity,
                request = request,
            )
            val credential = response.credential
            if (
                credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                throw IllegalStateException(AppText.t("auth_error_unsupported_credential"))
            }

            val googleCredential = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (error: GoogleIdTokenParsingException) {
                throw IllegalStateException(AppText.t("auth_error_parse_google_id_token"), error)
            }

            val now = System.currentTimeMillis()
            val existing = session.first()
            val providerSubject = GoogleIdTokenSubjectParser.parseSubject(googleCredential.idToken)
                ?: googleCredential.id
            val nextSession = UserSession(
                userId = existing?.userId ?: UUID.randomUUID().toString(),
                provider = "google",
                providerSubject = providerSubject,
                email = googleCredential.id.takeIf { it.isNotBlank() },
                displayName = googleCredential.displayName?.takeIf { it.isNotBlank() },
                avatarUrl = googleCredential.profilePictureUri?.toString(),
                createdAt = existing?.createdAt ?: now,
                lastSignedInAt = now,
            )
            saveSession(nextSession)
            nextSession
        }.recoverCatching { error ->
            if (error is GetCredentialException) {
                throw IllegalStateException(AppText.t("auth_error_google_sign_in_unavailable"), error)
            }
            throw error
        }
    }

    override suspend fun signOut() {
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
        saveSession(null)
    }

    override suspend fun deleteAccount() {
        signOut()
    }

    private suspend fun saveSession(session: UserSession?) {
        context.authDataStore.edit { preferences ->
            if (session == null) {
                preferences.remove(Keys.sessionJson)
            } else {
                preferences[Keys.sessionJson] = encodeSession(session)
            }
        }
    }

    private fun encodeSession(session: UserSession): String =
        JSONObject()
            .put("userId", session.userId)
            .put("provider", session.provider)
            .put("providerSubject", session.providerSubject)
            .put("email", session.email)
            .put("displayName", session.displayName)
            .put("avatarUrl", session.avatarUrl)
            .put("createdAt", session.createdAt)
            .put("lastSignedInAt", session.lastSignedInAt)
            .toString()

    private fun decodeSession(value: String?): UserSession? {
        if (value.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(value)
            UserSession(
                userId = json.getString("userId"),
                provider = json.getString("provider"),
                providerSubject = json.getString("providerSubject"),
                email = json.optString("email").takeIf { it.isNotBlank() && it != "null" },
                displayName = json.optString("displayName").takeIf { it.isNotBlank() && it != "null" },
                avatarUrl = json.optString("avatarUrl").takeIf { it.isNotBlank() && it != "null" },
                createdAt = json.getLong("createdAt"),
                lastSignedInAt = json.getLong("lastSignedInAt"),
            )
        }.getOrNull()
    }

    companion object {
        suspend fun clearStoredSession(context: Context) {
            context.authDataStore.edit { it.clear() }
        }
    }
}
