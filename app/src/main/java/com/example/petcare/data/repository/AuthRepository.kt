package com.example.petcare.data.repository

import com.example.petcare.data.model.UserProfile
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Repository untuk mengelola autentikasi pengguna (Login & Register).
 * Saat ini menggunakan data simulasi (mock) in-memory dan siap disambungkan
 * langsung ke Supabase Auth (`supabase.auth.signInWith(Email)` / `signUpWith(Email)`).
 */
class AuthRepository {

    private data class AccountRecord(
        val profile: UserProfile,
        val password: String
    )

    companion object {
        private val registeredAccounts = mutableListOf(
            AccountRecord(
                profile = UserProfile(
                    id = "user-demo-001",
                    fullName = "Tim PetCare",
                    email = "demo@petcare.com",
                    phoneNumber = "081234567890"
                ),
                password = "petcare123"
            )
        )

        private var activeUser: UserProfile? = null
    }

    suspend fun login(email: String, password: String): Result<UserProfile> {
        delay(700) // Simulasi jaringan
        val normalizedEmail = email.trim().lowercase()
        val matchedAccount = registeredAccounts.find {
            it.profile.email.lowercase() == normalizedEmail && it.password == password
        }

        return if (matchedAccount != null) {
            activeUser = matchedAccount.profile
            Result.success(matchedAccount.profile)
        } else {
            Result.failure(
                IllegalArgumentException("Email atau kata sandi salah. Silakan periksa kembali atau gunakan akun demo.")
            )
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<UserProfile> {
        delay(800) // Simulasi jaringan
        val normalizedEmail = email.trim().lowercase()
        val alreadyExists = registeredAccounts.any {
            it.profile.email.lowercase() == normalizedEmail
        }

        if (alreadyExists) {
            return Result.failure(
                IllegalArgumentException("Email sudah terdaftar. Silakan masuk menggunakan akun tersebut.")
            )
        }

        val newProfile = UserProfile(
            id = UUID.randomUUID().toString(),
            fullName = fullName.trim(),
            email = normalizedEmail,
            phoneNumber = phoneNumber.trim()
        )
        registeredAccounts.add(
            AccountRecord(
                profile = newProfile,
                password = password
            )
        )
        activeUser = newProfile
        return Result.success(newProfile)
    }

    fun getCurrentUser(): UserProfile? = activeUser

    fun logout() {
        activeUser = null
    }
}
