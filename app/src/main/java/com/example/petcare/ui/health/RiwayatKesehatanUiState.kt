package com.example.petcare.ui.health

import com.example.petcare.data.model.JenisRiwayatKesehatan
import com.example.petcare.data.model.RiwayatKesehatan

data class RiwayatKesehatanListUiState(
    val items: List<RiwayatKesehatan> = emptyList(),
    val allItemsCount: Int = 0,
    val sakitCount: Int = 0,
    val obatCount: Int = 0,
    val operasiCount: Int = 0,
    val selectedFilter: JenisRiwayatKesehatan = JenisRiwayatKesehatan.SEMUA,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


data class RiwayatKesehatanDetailUiState(
    val record: RiwayatKesehatan? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false
)


data class RiwayatKesehatanFormUiState(
    val petName: String = "",
    val petType: String = "Kucing",
    val jenis: JenisRiwayatKesehatan = JenisRiwayatKesehatan.SAKIT,
    val judul: String = "",
    val tanggal: String = "",
    val dokterAtauKlinik: String = "",
    val catatanMedis: String = "",
    val biaya: String = "",
    val statusPemulihan: String = "Selesai",
    val isEditMode: Boolean = false,
    val targetRecordId: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val validationError: String? = null
)
