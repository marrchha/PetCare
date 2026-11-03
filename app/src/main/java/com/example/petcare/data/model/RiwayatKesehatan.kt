package com.example.petcare.data.model

import java.util.UUID

enum class JenisRiwayatKesehatan(val label: String) {
    SEMUA("Semua"),
    SAKIT("Sakit"),
    OBAT("Obat"),
    OPERASI("Operasi");

    companion object {
        fun fromString(value: String): JenisRiwayatKesehatan {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: SAKIT
        }
    }
}

data class RiwayatKesehatan(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "user-demo-001",
    val petName: String,
    val petType: String = "Kucing",
    val jenis: JenisRiwayatKesehatan,
    val judul: String,
    val tanggal: String,
    val dokterAtauKlinik: String,
    val catatanMedis: String,
    val biaya: Long? = null,
    val statusPemulihan: String = "Selesai",
    val createdAt: Long = System.currentTimeMillis()
)
