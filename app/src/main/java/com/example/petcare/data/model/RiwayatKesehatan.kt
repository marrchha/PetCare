package com.example.petcare.data.model

import java.util.UUID

/**
 * Kategori jenis riwayat kesehatan hewan peliharaan.
 * Sesuai spesifikasi tugas: filter jenis (Sakit, Obat, Operasi) dan opsi Semua.
 */
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

/**
 * Model data rekam medis / riwayat kesehatan hewan peliharaan.
 * Merepresentasikan skema tabel Supabase `riwayat_kesehatan`.
 */
data class RiwayatKesehatan(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "user-demo-001",
    val petName: String,
    val petType: String = "Kucing", // Kucing, Anjing, Kelinci, Hamster, dll.
    val jenis: JenisRiwayatKesehatan,
    val judul: String, // Contoh: "Pemeriksaan Flu & Batuk", "Vaksin & Obat Cacing", "Operasi Steril"
    val tanggal: String, // Format tampilan: "24 Okt 2026" atau "2026-10-24"
    val dokterAtauKlinik: String, // Contoh: "drh. Sarah Wijaya - Klinik Satwa Medika"
    val catatanMedis: String, // Gejala, diagnosa dokter, resep dosis obat, atau instruksi perawatan
    val biaya: Long? = null, // Opsional: catatan estimasi biaya perawatan (IDR)
    val statusPemulihan: String = "Selesai", // "Dalam Perawatan", "Selesai", "Perlu Kontrol"
    val createdAt: Long = System.currentTimeMillis()
)
