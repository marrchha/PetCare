package com.example.petcare.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.UserProfile
import com.example.petcare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: UserProfile? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            currentUser = authRepository.getCurrentUser(),
            isAuthenticated = authRepository.getCurrentUser() != null
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun fillDemoAccount() {
        _uiState.update {
            it.copy(
                email = "demo@petcare.com",
                password = "petcare123",
                errorMessage = null
            )
        }
    }

    fun login() {
        val current = _uiState.value
        val email = current.email.trim()
        val password = current.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email dan kata sandi wajib diisi.")
            }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update {
                it.copy(errorMessage = "Format email tidak valid.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(email = email, password = password)
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = profile,
                            password = "",
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Gagal masuk ke akun."
                        )
                    }
                }
            )
        }
    }

    fun register() {
        val current = _uiState.value
        val fullName = current.fullName.trim()
        val email = current.email.trim()
        val phone = current.phoneNumber.trim()
        val password = current.password
        val confirmPassword = current.confirmPassword

        when {
            fullName.length < 3 -> {
                _uiState.update { it.copy(errorMessage = "Nama lengkap minimal 3 karakter.") }
                return
            }
            email.isBlank() || !isValidEmail(email) -> {
                _uiState.update { it.copy(errorMessage = "Masukkan alamat email yang valid.") }
                return
            }
            phone.length < 9 -> {
                _uiState.update { it.copy(errorMessage = "Masukkan nomor telepon/WhatsApp yang aktif.") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Kata sandi minimal terdiri dari 6 karakter.") }
                return
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Konfirmasi kata sandi tidak cocok.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                fullName = fullName,
                email = email,
                phoneNumber = phone,
                password = password
            )
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = profile,
                            password = "",
                            confirmPassword = "",
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Pendaftaran gagal."
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            AuthUiState(
                email = it.currentUser?.email.orEmpty(),
                isAuthenticated = false,
                currentUser = null
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
            (email.contains("@") && email.contains("."))
    }
}
