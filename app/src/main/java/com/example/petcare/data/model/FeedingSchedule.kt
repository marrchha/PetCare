package com.example.petcare.data.model

/**
 * Model data untuk Fitur 3: Jadwal Makan Harian (Jeanete Arthika Lorentz R.).
 * Menyimpan data jadwal makan hewan peliharaan.
 */
data class FeedingSchedule(
    val id: String = "",
    val petName: String = "",
    val time: String = "",
    val foodType: String = "",
    val portion: String = "",
    val notes: String = ""
)
