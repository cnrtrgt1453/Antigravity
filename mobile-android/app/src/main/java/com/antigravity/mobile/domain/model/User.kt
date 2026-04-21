package com.antigravity.mobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val profilePictureUrl: String?
)
