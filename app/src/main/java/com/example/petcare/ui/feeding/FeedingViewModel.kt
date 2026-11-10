package com.example.petcare.ui.feeding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.FeedingSchedule
import com.example.petcare.data.repository.FeedingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Satu UI state untuk fitur Jadwal Makan (List, Detail, dan Form). */
data class FeedingUiState(
    val schedules: List<FeedingSchedule> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val editingId: String? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val formError: String? = null,
    val infoMessage: String? = null
)

/**
 * ViewModel fitur Jadwal Makan Harian (MVVM + UDF).
 * Mengelola state dan logika bisnis untuk daftar, detail, serta form tambah/ubah jadwal makan.
 */
class FeedingViewModel(
    private val repository: FeedingRepository = FeedingRepository()
) : ViewModel() {

    var formPet by mutableStateOf("")
        private set
    var formTime by mutableStateOf("")
        private set
    var formFood by mutableStateOf("")
        private set
    var formPortion by mutableStateOf("")
        private set
    var formNotes by mutableStateOf("")
        private set

    private val _status = MutableStateFlow(FeedingUiState(isLoading = true))

    val uiState: StateFlow<FeedingUiState> = combine(
        repository.schedules, _status
    ) { daftar, status ->
        status.copy(schedules = daftar.sortedBy { it.time })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedingUiState(isLoading = true)
    )

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _status.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.update { it.copy(errorMessage = e.message ?: "Gagal memuat jadwal makan.") }
            } finally {
                _status.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPetChange(value: String) {
        formPet = value
        _status.update { it.copy(formError = null) }
    }

    fun onTimeChange(value: String) {
        formTime = value
        _status.update { it.copy(formError = null) }
    }

    fun onFoodChange(value: String) {
        formFood = value
        _status.update { it.copy(formError = null) }
    }

    fun onPortionChange(value: String) {
        formPortion = value
        _status.update { it.copy(formError = null) }
    }

    fun onNotesChange(value: String) {
        formNotes = value
        _status.update { it.copy(formError = null) }
    }

    fun prepareForm(id: String?) {
        val existing = id?.let { key -> repository.schedules.value.firstOrNull { it.id == key } }
        formPet = existing?.petName.orEmpty()
        formTime = existing?.time.orEmpty()
        formFood = existing?.foodType.orEmpty()
        formPortion = existing?.portion.orEmpty()
        formNotes = existing?.notes.orEmpty()
        _status.update { it.copy(editingId = existing?.id, formError = null, isSaved = false) }
    }

    fun saveSchedule() {
        val jam = parseTime(formTime)
        when {
            formPet.isBlank() -> {
                _status.update { it.copy(formError = "Nama hewan tidak boleh kosong.") }
                return
            }
            jam == null -> {
                _status.update { it.copy(formError = "Jam makan harus berformat HH:mm, contoh 07:30.") }
                return
            }
            formFood.isBlank() -> {
                _status.update { it.copy(formError = "Jenis makanan tidak boleh kosong.") }
                return
            }
            formPortion.isBlank() -> {
                _status.update { it.copy(formError = "Porsi tidak boleh kosong.") }
                return
            }
        }

        val editingId = _status.value.editingId
        viewModelScope.launch {
            _status.update { it.copy(isSaving = true, formError = null) }
            try {
                if (editingId == null) {
                    repository.add(formPet, jam, formFood, formPortion, formNotes)
                } else {
                    repository.update(editingId, formPet, jam, formFood, formPortion, formNotes)
                }
                _status.update {
                    it.copy(
                        isSaved = true,
                        infoMessage = if (editingId == null) "Jadwal makan ditambahkan." else "Jadwal makan diperbarui."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.update { it.copy(formError = e.message ?: "Gagal menyimpan jadwal.") }
            } finally {
                _status.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteSchedule(id: String) {
        viewModelScope.launch {
            _status.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                repository.delete(id)
                _status.update { it.copy(isDeleted = true, infoMessage = "Jadwal makan dihapus.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.update { it.copy(errorMessage = e.message ?: "Gagal menghapus jadwal.") }
            } finally {
                _status.update { it.copy(isSaving = false) }
            }
        }
    }

    fun consumeSaved() {
        _status.update { it.copy(isSaved = false) }
    }

    fun consumeDeleted() {
        _status.update { it.copy(isDeleted = false) }
    }

    fun consumeInfoMessage() {
        _status.update { it.copy(infoMessage = null) }
    }

    private fun parseTime(input: String): String? {
        val match = TIME_REGEX.matchEntire(input.trim()) ?: return null
        return "%02d:%s".format(match.groupValues[1].toInt(), match.groupValues[2])
    }

    private companion object {
        val TIME_REGEX = Regex("^([01]?\\d|2[0-3]):([0-5]\\d)$")
    }
}
