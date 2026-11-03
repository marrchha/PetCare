package com.example.petcare.data.model

data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val gender: String,
    val ageMonths: Int,
    val weightKg: Double,
    val color: String,
    val photoUri: String? = null,
    val notes: String = "",
    val accentIndex: Int = 0
)
