package com.rrrrz.tinyvow.data.auth

data class UserSession(
    val userId: String,
    val provider: String,
    val providerSubject: String,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAt: Long,
    val lastSignedInAt: Long,
)
