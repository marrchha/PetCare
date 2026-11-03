package com.example.petcare.ui.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.ClinicService
import com.example.petcare.data.model.Doctor
import com.example.petcare.data.repository.DoctorClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorClinicUiState(
    val isLoading: Boolean = false,
    val clinics: List<ClinicInfo> = emptyList(),
    val selectedClinicId: String = "",
    val doctors: List<Doctor> = emptyList(),
    val services: List<ClinicService> = emptyList(),
    val selectedDoctor: Doctor? = null,
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val filterBySelectedClinicOnly: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedClinic: ClinicInfo?
        get() = clinics.find { it.id == selectedClinicId } ?: clinics.firstOrNull()

    val filteredDoctors: List<Doctor>
        get() {
            var list = doctors
            if (filterBySelectedClinicOnly && selectedClinicId.isNotBlank()) {
                list = list.filter { it.clinicId == selectedClinicId }
            }
            if (searchQuery.isBlank()) return list
            val q = searchQuery.trim().lowercase()
            return list.filter {
                it.name.lowercase().contains(q) ||
                        it.specialization.lowercase().contains(q) ||
                        it.clinicName.lowercase().contains(q) ||
                        it.practiceDays.lowercase().contains(q)
            }
        }

    val filteredServices: List<ClinicService>
        get() {
            if (searchQuery.isBlank()) return services
            val q = searchQuery.trim().lowercase()
            return services.filter {
                it.name.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.description.lowercase().contains(q)
            }
        }
}

class DoctorClinicViewModel(
    private val repository: DoctorClinicRepository = DoctorClinicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorClinicUiState())
    val uiState: StateFlow<DoctorClinicUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.clinicsFlow.collect { list ->
                _uiState.update { it.copy(clinics = list) }
            }
        }
        viewModelScope.launch {
            repository.selectedClinicIdFlow.collect { id ->
                _uiState.update { it.copy(selectedClinicId = id) }
            }
        }
        viewModelScope.launch {
            repository.doctorsFlow.collect { list ->
                _uiState.update { it.copy(doctors = list) }
            }
        }
        viewModelScope.launch {
            repository.servicesFlow.collect { list ->
                _uiState.update { it.copy(services = list) }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectClinic(clinicId: String) {
        repository.selectClinic(clinicId)
    }

    fun toggleClinicFilter() {
        _uiState.update { it.copy(filterBySelectedClinicOnly = !it.filterBySelectedClinicOnly) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }

    fun loadDoctorDetail(doctorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val doctor = repository.getDoctorById(doctorId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    selectedDoctor = doctor
                )
            }
        }
    }

    fun saveDoctor(
        id: String?,
        name: String,
        specialization: String,
        practiceDays: String,
        practiceHours: String,
        phoneContact: String,
        clinicId: String,
        clinicName: String,
        photoUri: String?,
        avatarIndex: Int,
        isActive: Boolean,
        experienceYears: Int,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || specialization.isBlank() || phoneContact.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama, spesialisasi, dan kontak wajib diisi!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val doctor = Doctor(
                id = id?.takeIf { it.isNotBlank() } ?: java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                specialization = specialization.trim(),
                practiceDays = practiceDays.trim().ifBlank { "Senin - Jumat" },
                practiceHours = practiceHours.trim().ifBlank { "09:00 - 15:00" },
                phoneContact = phoneContact.trim(),
                clinicId = clinicId.ifBlank { _uiState.value.selectedClinicId },
                clinicName = clinicName.ifBlank { _uiState.value.selectedClinic?.name ?: "PetCare Clinic" },
                photoUri = photoUri,
                avatarIndex = avatarIndex,
                isActive = isActive,
                experienceYears = experienceYears,
                notes = notes.trim()
            )

            val result = if (id.isNullOrBlank()) {
                repository.addDoctor(doctor)
            } else {
                repository.updateDoctor(doctor)
            }

            result.fold(
                onSuccess = { savedDoc ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedDoctor = savedDoc,
                            userMessage = "Data ${savedDoc.name} berhasil disimpan."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal menyimpan dokter."
                        )
                    }
                }
            )
        }
    }

    fun toggleDoctorActiveStatus(doctorId: String) {
        viewModelScope.launch {
            val result = repository.toggleDoctorStatus(doctorId)
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            selectedDoctor = updated,
                            userMessage = "Status praktik ${updated.name}: " +
                                    if (updated.isActive) "Aktif Praktik" else "Libur / Cuti"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.localizedMessage ?: "Gagal mengubah status dokter.")
                    }
                }
            )
        }
    }

    fun deleteDoctor(doctorId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteDoctor(doctorId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedDoctor = null,
                            userMessage = "Data dokter berhasil dihapus."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal menghapus dokter."
                        )
                    }
                }
            )
        }
    }

    // Layanan
    fun saveService(
        id: String?,
        name: String,
        category: String,
        price: Long,
        durationMinutes: Int,
        description: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || price <= 0) {
            _uiState.update { it.copy(errorMessage = "Nama layanan dan harga harus valid!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val service = ClinicService(
                id = id?.takeIf { it.isNotBlank() } ?: java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                category = category.trim().ifBlank { "Umum" },
                estimatedPrice = price,
                durationMinutes = durationMinutes,
                description = description.trim()
            )

            val result = if (id.isNullOrBlank()) {
                repository.addService(service)
            } else {
                repository.updateService(service)
            }

            result.fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Layanan ${saved.name} berhasil disimpan."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal menyimpan layanan."
                        )
                    }
                }
            )
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            val result = repository.deleteService(serviceId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(userMessage = "Layanan berhasil dihapus.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Gagal menghapus.") }
                }
            )
        }
    }

    // Multi-Klinik CRUD
    fun saveClinic(
        id: String?,
        name: String,
        address: String,
        phone: String,
        hours: String,
        emergency: Boolean,
        description: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || address.isBlank() || phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama klinik, alamat, dan kontak wajib diisi!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val clinic = ClinicInfo(
                id = id?.takeIf { it.isNotBlank() } ?: java.util.UUID.randomUUID().toString(),
                name = name.trim(),
                address = address.trim(),
                phoneNumber = phone.trim(),
                operationalHours = hours.trim().ifBlank { "08:00 - 21:00 WIB" },
                emergencyAvailable = emergency,
                description = description.trim()
            )

            val result = if (id.isNullOrBlank()) {
                repository.addClinic(clinic)
            } else {
                repository.updateClinicInfo(clinic)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userMessage = "Data klinik ${clinic.name} berhasil disimpan."
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Gagal menyimpan klinik."
                        )
                    }
                }
            )
        }
    }

    fun deleteClinic(clinicId: String) {
        viewModelScope.launch {
            val result = repository.deleteClinic(clinicId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(userMessage = "Cabang klinik berhasil dihapus.") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Gagal menghapus klinik.") }
                }
            )
        }
    }
}
