package com.example.petcare.data.model

import java.util.UUID

/**
 * Model data informasi klinik untuk Fitur Manajemen Dokter Hewan & Klinik.
 * Merepresentasikan baris pada tabel Supabase: `klinik_info`.
 */
data class ClinicInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val phoneNumber: String = "",
    val operationalHours: String = "08:00 - 20:00 WIB",
    val emergencyAvailable: Boolean = true,
    val isActive: Boolean = true,
    val description: String = ""
)
