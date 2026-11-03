package com.example.petcare.data.model

/**
 * Representasi data profil pengguna aplikasi PetCare.
 * Dirancang agar mudah dipetakan ke tabel `profiles` / `auth.users` pada Supabase.
 */
data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String = "",
    val avatarUrl: String? = null
)
