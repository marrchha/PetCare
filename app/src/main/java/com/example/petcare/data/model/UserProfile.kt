package com.example.petcare.data.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String = "",
    val avatarUrl: String? = null
)
