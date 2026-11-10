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
import java.util.UUID

data class DoctorClinicUiState(
    val isLoading: Boolean = false,
    val clinics: List<ClinicInfo> = emptyList(),
    val selectedClinicId: String = "",
    val selectedClinicDetailId: String = "",
    val doctors: List<Doctor> = emptyList(),
    val services: List<ClinicService> = emptyList(),
    val selectedDoctor: Doctor? = null,

    // Tab halaman utama (0 = Dokter, 1 = Klinik & Layanan)
    val selectedTab: Int = 0,

    // Search & Filter Tab Dokter (Modal Bottom Sheet)
    val doctorSearchQuery: String = "",
    val isDoctorFilterSheetOpen: Boolean = false,
    val filterDoctorClinicId: String = "Semua",
    val filterDoctorSpecialization: String = "Semua",
    val filterDoctorStatus: String = "Semua",
    val filterDoctorDay: String = "Semua",
    val tempFilterDoctorClinicId: String = "Semua",
    val tempFilterDoctorSpecialization: String = "Semua",
    val tempFilterDoctorStatus: String = "Semua",
    val tempFilterDoctorDay: String = "Semua",

    // Search & Filter Tab Klinik
    val clinicSearchQuery: String = "",
    val filterClinicStatus: String = "Semua",

    // Halaman Detail Klinik (0 = Dokter Cabang, 1 = Layanan)
    val clinicDetailTab: Int = 0,
    val serviceSearchQuery: String = "",
    val isServiceFilterSheetOpen: Boolean = false,
    val filterServiceCategory: String = "Semua",
    val filterServiceStatus: String = "Semua",
    val tempFilterServiceCategory: String = "Semua",
    val tempFilterServiceStatus: String = "Semua",

    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedClinic: ClinicInfo?
        get() = clinics.find { it.id == selectedClinicId } ?: clinics.firstOrNull()

    val selectedClinicDetail: ClinicInfo?
        get() = clinics.find { it.id == selectedClinicDetailId } ?: selectedClinic

    val totalClinicsCount: Int
        get() = clinics.size

    val totalDoctorsCount: Int
        get() = doctors.size

    val activeDoctorsCount: Int
        get() = doctors.count { it.isActive }

    val activeDoctorFilterCount: Int
        get() = listOf(
            filterDoctorClinicId != "Semua",
            filterDoctorSpecialization != "Semua",
            filterDoctorStatus != "Semua",
            filterDoctorDay != "Semua"
        ).count { it }

    val filteredDoctors: List<Doctor>
        get() {
            val q = doctorSearchQuery.trim().lowercase()
            return doctors.filter { doc ->
                val matchesSearch = q.isEmpty() ||
                    doc.name.lowercase().contains(q) ||
                    doc.specialization.lowercase().contains(q) ||
                    doc.clinicName.lowercase().contains(q)

                val matchesClinic = filterDoctorClinicId == "Semua" ||
                    doc.clinicId == filterDoctorClinicId ||
                    doc.clinicName.equals(filterDoctorClinicId, ignoreCase = true)

                val matchesSpec = filterDoctorSpecialization == "Semua" ||
                    doc.specialization.contains(filterDoctorSpecialization, ignoreCase = true)

                val matchesStatus = when (filterDoctorStatus) {
                    "Aktif" -> doc.isActive
                    "Nonaktif" -> !doc.isActive
                    else -> true
                }

                val matchesDay = filterDoctorDay == "Semua" ||
                    doc.practiceDays.contains(filterDoctorDay, ignoreCase = true)

                matchesSearch && matchesClinic && matchesSpec && matchesStatus && matchesDay
            }
        }

    val filteredClinics: List<ClinicInfo>
        get() {
            val q = clinicSearchQuery.trim().lowercase()
            return clinics.filter { clinic ->
                val matchesSearch = q.isEmpty() ||
                    clinic.name.lowercase().contains(q) ||
                    clinic.address.lowercase().contains(q)

                val matchesStatus = when (filterClinicStatus) {
                    "Aktif" -> clinic.isActive
                    "Nonaktif" -> !clinic.isActive
                    else -> true
                }

                matchesSearch && matchesStatus
            }
        }

    val clinicDetailDoctors: List<Doctor>
        get() = doctors.filter { it.clinicId == selectedClinicDetailId }

    val activeServiceFilterCount: Int
        get() = listOf(
            filterServiceCategory != "Semua",
            filterServiceStatus != "Semua"
        ).count { it }

    val filteredClinicServices: List<ClinicService>
        get() {
            val q = serviceSearchQuery.trim().lowercase()
            return services.filter { srv ->
                val matchesClinic = srv.clinicId == selectedClinicDetailId
                val matchesSearch = q.isEmpty() ||
                    srv.name.lowercase().contains(q) ||
                    srv.description.lowercase().contains(q) ||
                    srv.category.lowercase().contains(q)
                val matchesCategory = filterServiceCategory == "Semua" ||
                    srv.category.equals(filterServiceCategory, ignoreCase = true)
                val matchesStatus = when (filterServiceStatus) {
                    "Aktif" -> srv.isActive
                    "Nonaktif" -> !srv.isActive
                    else -> true
                }
                matchesClinic && matchesSearch && matchesCategory && matchesStatus
            }
        }

    fun doctorCountForClinic(clinicId: String): Int {
        return doctors.count { it.clinicId == clinicId }
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
                _uiState.update { state ->
                    val defaultDetailId = state.selectedClinicDetailId.ifBlank { list.firstOrNull()?.id.orEmpty() }
                    state.copy(
                        clinics = list,
                        selectedClinicDetailId = defaultDetailId
                    )
                }
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

    // ==========================================
    // TAB & SEARCH HALAMAN UTAMA
    // ==========================================

    fun onTabChange(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onDoctorSearchChange(query: String) {
        _uiState.update { it.copy(doctorSearchQuery = query) }
    }

    fun onClinicSearchChange(query: String) {
        _uiState.update { it.copy(clinicSearchQuery = query) }
    }

    fun onClinicStatusFilterChange(status: String) {
        _uiState.update { it.copy(filterClinicStatus = status) }
    }

    // ==========================================
    // FILTER BOTTOM SHEET DOKTER (AND LOGIC)
    // ==========================================

    fun openDoctorFilterSheet() {
        _uiState.update {
            it.copy(
                isDoctorFilterSheetOpen = true,
                tempFilterDoctorClinicId = it.filterDoctorClinicId,
                tempFilterDoctorSpecialization = it.filterDoctorSpecialization,
                tempFilterDoctorStatus = it.filterDoctorStatus,
                tempFilterDoctorDay = it.filterDoctorDay
            )
        }
    }

    fun closeDoctorFilterSheet() {
        _uiState.update { it.copy(isDoctorFilterSheetOpen = false) }
    }

    fun onTempDoctorClinicChange(clinicId: String) {
        _uiState.update { it.copy(tempFilterDoctorClinicId = clinicId) }
    }

    fun onTempDoctorSpecializationChange(spec: String) {
        _uiState.update { it.copy(tempFilterDoctorSpecialization = spec) }
    }

    fun onTempDoctorStatusChange(status: String) {
        _uiState.update { it.copy(tempFilterDoctorStatus = status) }
    }

    fun onTempDoctorDayChange(day: String) {
        _uiState.update { it.copy(tempFilterDoctorDay = day) }
    }

    fun applyDoctorFilter() {
        _uiState.update {
            it.copy(
                filterDoctorClinicId = it.tempFilterDoctorClinicId,
                filterDoctorSpecialization = it.tempFilterDoctorSpecialization,
                filterDoctorStatus = it.tempFilterDoctorStatus,
                filterDoctorDay = it.tempFilterDoctorDay,
                isDoctorFilterSheetOpen = false
            )
        }
    }

    fun resetDoctorFilter() {
        _uiState.update {
            it.copy(
                filterDoctorClinicId = "Semua",
                filterDoctorSpecialization = "Semua",
                filterDoctorStatus = "Semua",
                filterDoctorDay = "Semua",
                tempFilterDoctorClinicId = "Semua",
                tempFilterDoctorSpecialization = "Semua",
                tempFilterDoctorStatus = "Semua",
                tempFilterDoctorDay = "Semua",
                isDoctorFilterSheetOpen = false
            )
        }
    }

    // ==========================================
    // DETAIL KLINIK & FILTER LAYANAN
    // ==========================================

    fun selectClinic(clinicId: String) {
        repository.selectClinic(clinicId)
        _uiState.update { it.copy(selectedClinicDetailId = clinicId) }
    }

    fun selectClinicDetail(clinicId: String) {
        repository.selectClinic(clinicId)
        _uiState.update {
            it.copy(
                selectedClinicDetailId = clinicId,
                serviceSearchQuery = "",
                filterServiceCategory = "Semua",
                filterServiceStatus = "Semua"
            )
        }
    }

    fun onClinicDetailTabChange(tabIndex: Int) {
        _uiState.update { it.copy(clinicDetailTab = tabIndex) }
    }

    fun onServiceSearchChange(query: String) {
        _uiState.update { it.copy(serviceSearchQuery = query) }
    }

    fun openServiceFilterSheet() {
        _uiState.update {
            it.copy(
                isServiceFilterSheetOpen = true,
                tempFilterServiceCategory = it.filterServiceCategory,
                tempFilterServiceStatus = it.filterServiceStatus
            )
        }
    }

    fun closeServiceFilterSheet() {
        _uiState.update { it.copy(isServiceFilterSheetOpen = false) }
    }

    fun onTempServiceCategoryChange(category: String) {
        _uiState.update { it.copy(tempFilterServiceCategory = category) }
    }

    fun onTempServiceStatusChange(status: String) {
        _uiState.update { it.copy(tempFilterServiceStatus = status) }
    }

    fun applyServiceFilter() {
        _uiState.update {
            it.copy(
                filterServiceCategory = it.tempFilterServiceCategory,
                filterServiceStatus = it.tempFilterServiceStatus,
                isServiceFilterSheetOpen = false
            )
        }
    }

    fun resetServiceFilter() {
        _uiState.update {
            it.copy(
                filterServiceCategory = "Semua",
                filterServiceStatus = "Semua",
                tempFilterServiceCategory = "Semua",
                tempFilterServiceStatus = "Semua",
                isServiceFilterSheetOpen = false
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(userMessage = null, errorMessage = null) }
    }

    // ==========================================
    // CRUD DOKTER
    // ==========================================

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
        photoUri: String? = null,
        avatarIndex: Int = 1,
        isActive: Boolean = true,
        experienceYears: Int = 3,
        notes: String = "",
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || specialization.isBlank() || phoneContact.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama, spesialisasi, dan kontak wajib diisi!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val resolvedClinic = _uiState.value.clinics.find { it.id == clinicId }
            val doctor = Doctor(
                id = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                name = name.trim(),
                specialization = specialization.trim(),
                practiceDays = practiceDays.trim().ifBlank { "Senin, Rabu, Jumat" },
                practiceHours = practiceHours.trim().ifBlank { "08:00 - 14:00" },
                phoneContact = phoneContact.trim(),
                clinicId = clinicId.ifBlank { _uiState.value.selectedClinicDetailId.ifBlank { "clinic-001" } },
                clinicName = resolvedClinic?.name ?: clinicName.ifBlank { "Petcare Malang" },
                photoUri = null,
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
                                if (updated.isActive) "Praktik Aktif" else "Nonaktif / Cuti"
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

    // ==========================================
    // CRUD LAYANAN KLINIK
    // ==========================================

    fun saveService(
        id: String?,
        clinicId: String = _uiState.value.selectedClinicDetailId.ifBlank { "clinic-001" },
        name: String,
        category: String,
        price: Long,
        durationMinutes: Int,
        description: String,
        isActive: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        if (name.isBlank() || price <= 0) {
            _uiState.update { it.copy(errorMessage = "Nama layanan dan harga harus valid!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val service = ClinicService(
                id = id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                clinicId = clinicId,
                name = name.trim(),
                category = category.trim().ifBlank { "Pemeriksaan" },
                estimatedPrice = price,
                durationMinutes = durationMinutes,
                description = description.trim(),
                isActive = isActive
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
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Gagal menghapus layanan.") }
                }
            )
        }
    }

    // ==========================================
    // CRUD MULTI-KLINIK
    // ==========================================

    fun saveClinic(
        id: String?,
        name: String,
        address: String,
        phone: String,
        hours: String,
        emergency: Boolean,
        isActive: Boolean = true,
        description: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (name.isBlank() || address.isBlank() || phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama klinik, alamat, dan kontak wajib diisi!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val clinic = ClinicInfo(
                id = id?.takeIf { it.isNotBlank() } ?: "clinic-${UUID.randomUUID().toString().take(8)}",
                name = name.trim(),
                address = address.trim(),
                phoneNumber = phone.trim(),
                operationalHours = hours.trim().ifBlank { "08:00 - 21:00 WIB" },
                emergencyAvailable = emergency,
                isActive = isActive,
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
                            selectedClinicDetailId = clinic.id,
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
