package com.example.petcare.ui.doctor

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.petcare.R

@Composable
fun DoctorPhotoThumbnail(
    photoUri: String?,
    avatarIndex: Int = 1,
    size: Dp = 50.dp,
    borderColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap = remember(photoUri) {
        if (!photoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(photoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val defaultAvatarRes = when (avatarIndex) {
        1 -> R.drawable.ic_avatar_doctor_female_1
        2 -> R.drawable.ic_avatar_doctor_male_1
        3 -> R.drawable.ic_avatar_doctor_female_2
        4 -> R.drawable.ic_avatar_doctor_male_2
        else -> R.drawable.ic_avatar_doctor_female_1
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto Dokter",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = defaultAvatarRes),
                contentDescription = "Foto Dokter",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
