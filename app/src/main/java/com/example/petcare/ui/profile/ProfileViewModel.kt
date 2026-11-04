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
 * - Perubahan data dilakukan lewat Repository (Single Source of Truth).
 * - ViewModel tidak menyimpan Context; AvatarStorage dikirim dari View sebagai parameter.
 */
class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // (1) Teks TextField -> mutableStateOf (pemiliknya: ketikan user), diubah hanya dari ViewModel
    var editName by mutableStateOf("")
        private set
    var editPhone by mutableStateOf("")
        private set

    // (2) Status layar yang diatur ViewModel sendiri (edit, loading, error, pesan)
    private val _status = MutableStateFlow(ProfileUiState())

    // (3) UI state final = data pengguna dari Repository + status layar.
    // combine dijalankan ulang setiap kali SALAH SATU sumber berubah.
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
        // Validasi dilakukan di ViewModel (logika bisnis), bukan di Composable
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
                _status.update { it.copy(isEditing = false, infoMessage = "Profil berhasil diperbarui.") }
            } catch (e: CancellationException) {
                throw e // pembatalan coroutine jangan ditangkap
            } catch (e: Exception) {
                _status.update { it.copy(errorMessage = e.message ?: "Gagal menyimpan profil.") }
            } finally {
                _status.update { it.copy(isSaving = false) } // selalu dijalankan
            }
        }
    }

    /** Upload foto profil. Proses file dijalankan di Dispatchers.IO (main-safe). */
    fun onPhotoPicked(uri: Uri, avatarStorage: AvatarStorage) {
        val user = repository.getCurrentUser() ?: return
        viewModelScope.launch {
            _status.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            try {
                val file = withContext(Dispatchers.IO) { avatarStorage.save(user.id, uri) }
                repository.setAvatar(file.absolutePath)
                _status.update { it.copy(infoMessage = "Foto profil berhasil diperbarui.") }
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

    /** Membaca kembali foto yang pernah diunggah (dipanggil dari LaunchedEffect saat user login). */
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

    /** Dipanggil saat logout agar state edit tidak terbawa ke akun lain. */
    fun resetState() {
        editName = ""
        editPhone = ""
        _status.value = ProfileUiState()
    }
}