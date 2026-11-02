package com.example.petcare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = TealOn,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = Honey40,
    onSecondary = HoneyOn,
    secondaryContainer = HoneyContainer,
    onSecondaryContainer = OnHoneyContainer,
    tertiary = Rose40,
    onTertiary = RoseOn,
    tertiaryContainer = RoseContainer,
    onTertiaryContainer = OnRoseContainer,
    background = Mint99,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = ContainerLowestLight,
    surfaceContainerLow = ContainerLowLight,
    surfaceContainer = ContainerLight,
    surfaceContainerHigh = ContainerHighLight,
    surfaceContainerHighest = ContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColors = darkColorScheme(
    primary = TealDark80,
    onPrimary = OnTealDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = HoneyDark80,
    onSecondary = OnHoneyDark,
    secondaryContainer = HoneyContainerDark,
    onSecondaryContainer = OnHoneyContainerDark,
    tertiary = RoseDark80,
    onTertiary = OnRoseDark,
    tertiaryContainer = RoseContainerDark,
    onTertiaryContainer = OnRoseContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = ContainerLowestDark,
    surfaceContainerLow = ContainerLowDark,
    surfaceContainer = ContainerDark,
    surfaceContainerHigh = ContainerHighDark,
    surfaceContainerHighest = ContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

/**
 * Bungkus seluruh aplikasi dengan PetCareTheme { ... } di MainActivity.
 * Dynamic color sengaja tidak dipakai agar warna sama di semua HP anggota.
 */
@Composable
fun PetCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}