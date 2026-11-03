package com.example.petcare.data.model

data class Pet(
    val id: String,
    val name: String,
    val species: String,       // Contoh: "Kucing", "Anjing", "Kelinci", "Burung"
    val breed: String,         // Contoh: "British Shorthair", "Golden Retriever"
    val gender: String,        // "Jantan" atau "Betina"
    val ageMonths: Int,        // Umur dalam bulan
    val weightKg: Double,      // Berat badan (kg)
    val color: String,         // Warna bulu / ciri fisik
    val photoUri: String? = null, // URI foto dari galeri atau URL Supabase Storage
    val notes: String = "",    // Catatan khusus (alergi, makanan favorit, karakter)
    val accentIndex: Int = 0   // Variasi warna kartu pastel (0 = Lavender, 1 = Peach, 2 = Sky)
)
