package com.example.petcare.data.model

import java.util.UUID

/**
 * Model data layanan klinik untuk Fitur Manajemen Dokter Hewan & Klinik.
 * Merepresentasikan baris pada tabel Supabase: `layanan_klinik`.
 */
data class ClinicService(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val estimatedPrice: Long,
    val durationMinutes: Int,
    val category: String = "Umum"
)
