package com.rrrrz.tinyvow.data.auth

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val session: Flow<UserSession?>
    val isGoogleSignInConfigured: Boolean

    suspend fun ensureLocalSession(): UserSession
    suspend fun signInWithGoogle(activity: ComponentActivity): Result<UserSession>
    suspend fun signOut()
    suspend fun deleteAccount()
}
