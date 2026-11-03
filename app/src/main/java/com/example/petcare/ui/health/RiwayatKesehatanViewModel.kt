package com.example.petcare.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.JenisRiwayatKesehatan
import com.example.petcare.data.model.RiwayatKesehatan
import com.example.petcare.data.repository.RiwayatKesehatanRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RiwayatKesehatanViewModel(
    private val repository: RiwayatKesehatanRepository = RiwayatKesehatanRepository()
) : ViewModel() {

    private val _listUiState = MutableStateFlow(RiwayatKesehatanListUiState())
    val listUiState: StateFlow<RiwayatKesehatanListUiState> = _listUiState.asStateFlow()

    private val _detailUiState = MutableStateFlow(RiwayatKesehatanDetailUiState())
    val detailUiState: StateFlow<RiwayatKesehatanDetailUiState> = _detailUiState.asStateFlow()

    private val _formUiState = MutableStateFlow(RiwayatKesehatanFormUiState())
    val formUiState: StateFlow<RiwayatKesehatanFormUiState> = _formUiState.asStateFlow()

    private var listObservationJob: Job? = null

    init {
        observeList()
        observeCounts()
    }

    private fun observeList() {
        listObservationJob?.cancel()
        listObservationJob = repository.getRiwayatList(
            filterJenis = _listUiState.value.selectedFilter,
            searchQuery = _listUiState.value.searchQuery
        ).onEach { records ->
            _listUiState.update { it.copy(items = records, isLoading = false) }
        }.launchIn(viewModelScope)
    }


    private fun observeCounts() {
        repository.getRiwayatList(JenisRiwayatKesehatan.SEMUA, "").onEach { all ->
            val sakit = all.count { it.jenis == JenisRiwayatKesehatan.SAKIT }
            val obat = all.count { it.jenis == JenisRiwayatKesehatan.OBAT }
            val operasi = all.count { it.jenis == JenisRiwayatKesehatan.OPERASI }

            _listUiState.update {
                it.copy(
                    allItemsCount = all.size,
                    sakitCount = sakit,
                    obatCount = obat,
                    operasiCount = operasi
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onFilterChange(filter: JenisRiwayatKesehatan) {
        _listUiState.update { it.copy(selectedFilter = filter) }
        observeList()
    }

    fun onSearchQueryChange(query: String) {
        _listUiState.update { it.copy(searchQuery = query) }
        observeList()
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detailUiState.update { it.copy(isLoading = true, errorMessage = null, isDeleted = false) }
            val item = repository.getRiwayatById(id)
            if (item != null) {
                _detailUiState.update { it.copy(record = item, isLoading = false) }
            } else {
                _detailUiState.update {
                    it.copy(
                        record = null,
                        isLoading = false,
                        errorMessage = "Catatan medis tidak ditemukan."
                    )
                }
            }
        }
    }


    fun prepareFormForCreate() {
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        _formUiState.value = RiwayatKesehatanFormUiState(
            petName = "",
            petType = "Kucing",
            jenis = JenisRiwayatKesehatan.SAKIT,
            judul = "",
            tanggal = todayStr,
            dokterAtauKlinik = "",
            catatanMedis = "",
            biaya = "",
            statusPemulihan = "Selesai",
            isEditMode = false,
            targetRecordId = null,
            validationError = null,
            isSuccess = false,
            isLoading = false
        )
    }


    fun prepareFormForEdit(id: String) {
        viewModelScope.launch {
            _formUiState.update { it.copy(isLoading = true, validationError = null) }
            val item = repository.getRiwayatById(id)
            if (item != null) {
                _formUiState.value = RiwayatKesehatanFormUiState(
                    petName = item.petName,
                    petType = item.petType,
                    jenis = item.jenis,
                    judul = item.judul,
                    tanggal = item.tanggal,
                    dokterAtauKlinik = item.dokterAtauKlinik,
                    catatanMedis = item.catatanMedis,
                    biaya = item.biaya?.toString() ?: "",
                    statusPemulihan = item.statusPemulihan,
                    isEditMode = true,
                    targetRecordId = item.id,
                    isLoading = false,
                    isSuccess = false,
                    validationError = null
                )
            } else {
                _formUiState.update {
                    it.copy(
                        isLoading = false,
                        validationError = "Data tidak dapat dimuat untuk diedit."
                    )
                }
            }
        }
    }

    // Form input
    fun onPetNameChange(value: String) {
        _formUiState.update { it.copy(petName = value, validationError = null) }
    }

    fun onPetTypeChange(value: String) {
        _formUiState.update { it.copy(petType = value, validationError = null) }
    }

    fun onJenisChange(value: JenisRiwayatKesehatan) {
        _formUiState.update { it.copy(jenis = value, validationError = null) }
    }

    fun onJudulChange(value: String) {
        _formUiState.update { it.copy(judul = value, validationError = null) }
    }

    fun onTanggalChange(value: String) {
        _formUiState.update { it.copy(tanggal = value, validationError = null) }
    }

    fun onDokterChange(value: String) {
        _formUiState.update { it.copy(dokterAtauKlinik = value, validationError = null) }
    }

    fun onCatatanMedisChange(value: String) {
        _formUiState.update { it.copy(catatanMedis = value, validationError = null) }
    }

    fun onBiayaChange(value: String) {
        val cleanDigits = value.filter { it.isDigit() }
        _formUiState.update { it.copy(biaya = cleanDigits, validationError = null) }
    }

    fun onStatusPemulihanChange(value: String) {
        _formUiState.update { it.copy(statusPemulihan = value, validationError = null) }
    }


    fun saveForm(onSuccess: (savedId: String) -> Unit) {
        val state = _formUiState.value
        val petName = state.petName.trim()
        val judul = state.judul.trim()
        val tanggal = state.tanggal.trim()
        val dokter = state.dokterAtauKlinik.trim()
        val catatan = state.catatanMedis.trim()

        // Validasi input
        when {
            petName.isBlank() -> {
                _formUiState.update { it.copy(validationError = "Nama hewan peliharaan wajib diisi.") }
                return
            }
            judul.isBlank() -> {
                _formUiState.update { it.copy(validationError = "Judul / Diagnosa / Keluhan medis wajib diisi.") }
                return
            }
            tanggal.isBlank() -> {
                _formUiState.update { it.copy(validationError = "Tanggal pemeriksaan wajib diisi.") }
                return
            }
            dokter.isBlank() -> {
                _formUiState.update { it.copy(validationError = "Nama dokter atau klinik wajib diisi.") }
                return
            }
            catatan.isBlank() -> {
                _formUiState.update { it.copy(validationError = "Catatan medis atau instruksi resep wajib diisi.") }
                return
            }
        }

        val parsedBiaya = state.biaya.toLongOrNull()

        viewModelScope.launch {
            _formUiState.update { it.copy(isLoading = true, validationError = null) }

            val itemToSave = if (state.isEditMode && !state.targetRecordId.isNullOrBlank()) {
                RiwayatKesehatan(
                    id = state.targetRecordId,
                    petName = petName,
                    petType = state.petType,
                    jenis = state.jenis,
                    judul = judul,
                    tanggal = tanggal,
                    dokterAtauKlinik = dokter,
                    catatanMedis = catatan,
                    biaya = parsedBiaya,
                    statusPemulihan = state.statusPemulihan
                )
            } else {
                RiwayatKesehatan(
                    id = UUID.randomUUID().toString(),
                    petName = petName,
                    petType = state.petType,
                    jenis = state.jenis,
                    judul = judul,
                    tanggal = tanggal,
                    dokterAtauKlinik = dokter,
                    catatanMedis = catatan,
                    biaya = parsedBiaya,
                    statusPemulihan = state.statusPemulihan
                )
            }

            val result = if (state.isEditMode) {
                repository.updateRiwayat(itemToSave)
            } else {
                repository.insertRiwayat(itemToSave)
            }

            result.fold(
                onSuccess = { savedItem ->
                    _formUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    // Update juga detail state jika sedang membuka detail record ini
                    _detailUiState.update { it.copy(record = savedItem) }
                    onSuccess(savedItem.id)
                },
                onFailure = { error ->
                    _formUiState.update {
                        it.copy(
                            isLoading = false,
                            validationError = error.message ?: "Gagal menyimpan catatan riwayat kesehatan."
                        )
                    }
                }
            )
        }
    }

    /**
     * Menghapus catatan riwayat kesehatan (DELETE).
     */
    fun deleteRecord(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _detailUiState.update { it.copy(isDeleting = true, errorMessage = null) }
            val result = repository.deleteRiwayat(id)
            result.fold(
                onSuccess = {
                    _detailUiState.update { it.copy(isDeleting = false, isDeleted = true, record = null) }
                    onSuccess()
                },
                onFailure = { error ->
                    _detailUiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "Gagal menghapus catatan riwayat kesehatan."
                        )
                    }
                }
            )
        }
    }
}
