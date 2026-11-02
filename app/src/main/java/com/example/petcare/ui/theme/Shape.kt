package com.example.petcare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Aturan kelompok: kartu & tombol memakai sudut 12-16dp.
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chip kecil, badge
    small = RoundedCornerShape(12.dp),       // tombol, text field
    medium = RoundedCornerShape(16.dp),      // kartu
    large = RoundedCornerShape(24.dp),       // bottom sheet, dialog
    extraLarge = RoundedCornerShape(32.dp)
)