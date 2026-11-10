package com.example.petcare.ui.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.local.AvatarStorage
import com.example.petcare.data.model.UserProfile
import com.example.petcare.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Satu UI state untuk satu layar Profil.
 * Field `user` diisi oleh combine (dari Repository), field lain diatur ViewModel sendiri.
 */
data class ProfileUiState(
    val user: UserProfile? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,          // sedang menyimpan perubahan nama/telepon
    val isUploadingPhoto: Boolean = false,  // sedang memproses foto
    val errorMessage: String? = null,       // null = tidak ada error
    val infoMessage: String? = null         // pesan sukses (ditampilkan lewat Snackbar)
)

/**
 * ViewModel halaman Profil (MVVM + UDF):
 * - View membaca `uiState`, lalu mengirim event dengan memanggil fungsi di ViewModel.
 * - Perubahan data dilakukan lewat Repository (Single Source of Truth) dan tersinkron ke Supabase.
 */
class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var editName by mutableStateOf("")
        private set
    var editPhone by mutableStateOf("")
        private set

    private val _status = MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.currentUserFlow, _status
    ) { user, status ->
        status.copy(user = user)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(user = repository.getCurrentUser())
    )

    fun onNameChange(value: String) {
        editName = value
        _status.update { it.copy(errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        editPhone = value
        _status.update { it.copy(errorMessage = null) }
    }

    fun startEdit() {
        val user = repository.getCurrentUser() ?: return
        editName = user.fullName
        editPhone = user.phoneNumber
        _status.update { it.copy(isEditing = true, errorMessage = null) }
    }

    fun cancelEdit() {
        _status.update { it.copy(isEditing = false, errorMessage = null) }
    }

    fun saveProfile() {
        if (editName.trim().length < 3) {
            _status.update { it.copy(errorMessage = "Nama lengkap minimal 3 karakter.") }
            return
        }
        if (editPhone.trim().length < 9) {
            _status.update { it.copy(errorMessage = "Masukkan nomor telepon/WhatsApp yang aktif.") }
            return
        }

        viewModelScope.launch {
            _status.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.updateProfile(editName, editPhone).getOrThrow()
                _status.update { it.copy(isEditing = false, infoMessage = "Profil berhasil diperbarui di Supabase.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.update { it.copy(errorMessage = e.message ?: "Gagal menyimpan profil.") }
            } finally {
                _status.update { it.copy(isSaving = false) }
            }
        }
    }

    /** Upload foto profil ke Supabase Storage dan simpan URL-nya ke tabel users. */
    fun onPhotoPicked(uri: Uri, avatarStorage: AvatarStorage) {
        val user = repository.getCurrentUser() ?: return
        viewModelScope.launch {
            _status.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            try {
                val uploadedUrlOrPath = withContext(Dispatchers.IO) {
                    avatarStorage.saveAndUpload(user.id, uri)
                }
                repository.setAvatar(uploadedUrlOrPath)
                _status.update { it.copy(infoMessage = "Foto profil berhasil diunggah ke Supabase.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.update { it.copy(errorMessage = "Gagal memproses foto. Coba pilih gambar lain.") }
            } finally {
                _status.update { it.copy(isUploadingPhoto = false) }
            }
        }
    }

    fun removePhoto(avatarStorage: AvatarStorage) {
        val user = repository.getCurrentUser() ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { avatarStorage.delete(user.id) }
            repository.setAvatar(null)
            _status.update { it.copy(infoMessage = "Foto profil dihapus.") }
        }
    }

    fun restoreAvatar(avatarStorage: AvatarStorage) {
        val user = repository.getCurrentUser() ?: return
        if (user.avatarUrl != null) return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { avatarStorage.find(user.id) }
            if (file != null) repository.setAvatar(file.absolutePath)
        }
    }

    fun consumeInfoMessage() {
        _status.update { it.copy(infoMessage = null) }
    }

    fun resetState() {
        editName = ""
        editPhone = ""
        _status.value = ProfileUiState()
    }
}
