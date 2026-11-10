package com.example.petcare.data.repository

import com.example.petcare.data.model.UserProfile
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

class AuthRepository {

    private data class AccountRecord(
        val profile: UserProfile,
        val password: String
    )

    companion object {
        private const val TABLE_USERS = "users"

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
        val normalizedEmail = email.trim().lowercase()

        if (SupabaseConfig.isConfigured) {
            val encodedEmail = SupabaseRestClient.encodeParam(normalizedEmail)
            val remoteResult = SupabaseRestClient.getList(
                table = TABLE_USERS,
                queryParams = "select=*&email=eq.$encodedEmail"
            )

            remoteResult.getOrNull()?.let { arr ->
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    val dbPassword = obj.optString("password", "")
                    if (dbPassword == password) {
                        val profile = jsonToUserProfile(obj)
                        activeUser = profile
                        _currentUserFlow.value = profile
                        syncLocalCache(profile, password)
                        return Result.success(profile)
                    } else {
                        return Result.failure(
                            IllegalArgumentException("Email atau kata sandi salah. Silakan periksa kembali.")
                        )
                    }
                } else if (normalizedEmail == "demo@petcare.com" && password == "petcare123") {
                    val demoProfile = registeredAccounts.first().profile
                    SupabaseRestClient.insert(
                        table = TABLE_USERS,
                        payload = userProfileToJson(demoProfile, password)
                    )
                    activeUser = demoProfile
                    _currentUserFlow.value = demoProfile
                    return Result.success(demoProfile)
                } else {
                    return Result.failure(
                        IllegalArgumentException("Akun tidak ditemukan di database. Silakan daftar akun baru atau gunakan akun demo.")
                    )
                }
            }
        }

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
        val normalizedEmail = email.trim().lowercase()

        if (SupabaseConfig.isConfigured) {
            val encodedEmail = SupabaseRestClient.encodeParam(normalizedEmail)
            val checkExisting = SupabaseRestClient.getList(
                table = TABLE_USERS,
                queryParams = "select=id&email=eq.$encodedEmail"
            ).getOrNull()

            if (checkExisting != null && checkExisting.length() > 0) {
                return Result.failure(
                    IllegalArgumentException("Email sudah terdaftar di Supabase. Silakan masuk menggunakan akun tersebut.")
                )
            }
        } else {
            val alreadyExists = registeredAccounts.any {
                it.profile.email.lowercase() == normalizedEmail
            }
            if (alreadyExists) {
                return Result.failure(
                    IllegalArgumentException("Email sudah terdaftar. Silakan masuk menggunakan akun tersebut.")
                )
            }
        }

        val newProfile = UserProfile(
            id = "user-${UUID.randomUUID().toString().take(8)}",
            fullName = fullName.trim(),
            email = normalizedEmail,
            phoneNumber = phoneNumber.trim()
        )

        if (SupabaseConfig.isConfigured) {
            val insertRes = SupabaseRestClient.insert(
                table = TABLE_USERS,
                payload = userProfileToJson(newProfile, password),
                upsert = false
            )
            insertRes.onFailure { err ->
                return Result.failure(
                    IllegalStateException("Gagal mendaftarkan akun ke Supabase: ${err.message}")
                )
            }
        }

        syncLocalCache(newProfile, password)
        activeUser = newProfile
        _currentUserFlow.value = activeUser
        return Result.success(newProfile)
    }

    suspend fun updateProfile(fullName: String, phoneNumber: String): Result<UserProfile> {
        val current = activeUser ?: return Result.failure(IllegalStateException("Pengguna belum masuk"))
        val updated = current.copy(fullName = fullName.trim(), phoneNumber = phoneNumber.trim())

        if (SupabaseConfig.isConfigured) {
            val payload = JSONObject().apply {
                put("full_name", updated.fullName)
                put("phone_number", updated.phoneNumber)
            }
            val encodedId = SupabaseRestClient.encodeParam(current.id)
            SupabaseRestClient.update(
                table = TABLE_USERS,
                matchQuery = "id=eq.$encodedId",
                payload = payload
            ).onFailure { err ->
                return Result.failure(IllegalStateException("Gagal memperbarui profil di Supabase: ${err.message}"))
            }
        }

        activeUser = updated
        _currentUserFlow.value = updated

        val index = registeredAccounts.indexOfFirst { it.profile.id == current.id }
        if (index != -1) {
            registeredAccounts[index] = registeredAccounts[index].copy(profile = updated)
        }
        return Result.success(updated)
    }

    suspend fun setAvatar(avatarUrl: String?): Result<UserProfile> {
        val current = activeUser ?: return Result.failure(IllegalStateException("Pengguna belum masuk"))
        val updated = current.copy(avatarUrl = avatarUrl)

        if (SupabaseConfig.isConfigured) {
            val payload = JSONObject().apply {
                put("avatar_url", avatarUrl ?: JSONObject.NULL)
            }
            val encodedId = SupabaseRestClient.encodeParam(current.id)
            SupabaseRestClient.update(
                table = TABLE_USERS,
                matchQuery = "id=eq.$encodedId",
                payload = payload
            )
        }

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

    private fun syncLocalCache(profile: UserProfile, password: String) {
        val index = registeredAccounts.indexOfFirst { it.profile.id == profile.id }
        if (index != -1) {
            registeredAccounts[index] = AccountRecord(profile, password)
        } else {
            registeredAccounts.add(AccountRecord(profile, password))
        }
    }

    private fun jsonToUserProfile(obj: JSONObject): UserProfile {
        val rawAvatar = obj.optString("avatar_url", "")
        return UserProfile(
            id = obj.optString("id", UUID.randomUUID().toString()),
            fullName = obj.optString("full_name", "Pengguna PetCare"),
            email = obj.optString("email", ""),
            phoneNumber = obj.optString("phone_number", ""),
            avatarUrl = if (rawAvatar.isBlank() || rawAvatar == "null") null else rawAvatar
        )
    }

    private fun userProfileToJson(profile: UserProfile, password: String): JSONObject {
        return JSONObject().apply {
            put("id", profile.id)
            put("full_name", profile.fullName)
            put("email", profile.email)
            put("phone_number", profile.phoneNumber)
            put("password", password)
            put("avatar_url", profile.avatarUrl ?: JSONObject.NULL)
        }
    }
}
