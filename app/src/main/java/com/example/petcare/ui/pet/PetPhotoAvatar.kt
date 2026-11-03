package com.example.petcare.ui.pet

import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CrueltyFree
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PetPhotoAvatar(
    photoUri: String?,
    petName: String,
    species: String,
    containerColor: Color,
    contentColor: Color,
    iconSize: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap: ImageBitmap? = remember(photoUri) {
        if (photoUri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                val parsedUri = Uri.parse(photoUri)
                val source = ImageDecoder.createSource(context.contentResolver, parsedUri)
                ImageDecoder.decodeBitmap(source).asImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier.background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap,
                contentDescription = "Foto $petName",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(iconSize * 1.85f)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (species.lowercase()) {
                    "kelinci" -> Icons.Outlined.CrueltyFree
                    else -> Icons.Outlined.Pets
                }
                Icon(
                    imageVector = icon,
                    contentDescription = species,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
