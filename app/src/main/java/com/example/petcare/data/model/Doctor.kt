package com.example.petcare.data.model

import java.util.UUID

/**
 * Model data dokter hewan untuk Fitur Manajemen Dokter Hewan & Klinik.
 * Merepresentasikan baris pada tabel Supabase: `dokter`.
 */
data class Doctor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val specialization: String,
    val practiceDays: String,
    val practiceHours: String = "08:00 - 14:00",
    val phoneContact: String,
    val clinicId: String = "clinic-001",
    val clinicName: String = "Petcare Malang",
    val photoUri: String? = null,
    val avatarIndex: Int = 1,
    val isActive: Boolean = true,
    val experienceYears: Int = 3,
    val notes: String = ""
)
