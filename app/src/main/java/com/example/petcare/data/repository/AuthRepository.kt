package com.example.petcare.data.repository

import com.example.petcare.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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
        private val _currentUserFlow = MutableStateFlow<UserProfile?>(null)
        val currentUserFlow: StateFlow<UserProfile?> = _currentUserFlow.asStateFlow()
    }

    val currentUserFlow: StateFlow<UserProfile?> get() = Companion.currentUserFlow

    suspend fun login(email: String, password: String): Result<UserProfile> {
        delay(700)
        val normalizedEmail = email.trim().lowercase()
        val matchedAccount = registeredAccounts.find {
            it.profile.email.lowercase() == normalizedEmail && it.password == password
        }

        return if (matchedAccount != null) {
            activeUser = matchedAccount.profile
            _currentUserFlow.value = activeUser
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
        _currentUserFlow.value = activeUser
        return Result.success(newProfile)
    }

    suspend fun updateProfile(fullName: String, phoneNumber: String): Result<UserProfile> {
        delay(500)
        val current = activeUser ?: return Result.failure(IllegalStateException("Pengguna belum masuk"))
        val updated = current.copy(fullName = fullName.trim(), phoneNumber = phoneNumber.trim())
        activeUser = updated
        _currentUserFlow.value = updated

        val index = registeredAccounts.indexOfFirst { it.profile.id == current.id }
        if (index != -1) {
            registeredAccounts[index] = registeredAccounts[index].copy(profile = updated)
        }
        return Result.success(updated)
    }

    suspend fun setAvatar(avatarUrl: String?): Result<UserProfile> {
        delay(300)
        val current = activeUser ?: return Result.failure(IllegalStateException("Pengguna belum masuk"))
        val updated = current.copy(avatarUrl = avatarUrl)
        activeUser = updated
        _currentUserFlow.value = updated

        val index = registeredAccounts.indexOfFirst { it.profile.id == current.id }
        if (index != -1) {
            registeredAccounts[index] = registeredAccounts[index].copy(profile = updated)
        }
        return Result.success(updated)
    }

    fun getCurrentUser(): UserProfile? = activeUser

    fun logout() {
        activeUser = null
        _currentUserFlow.value = null
    }
}
