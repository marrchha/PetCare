package com.example.petcare.ui.feeding

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.FeedingSchedule
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.Spacing

/**
 * SCREEN: menghubungkan ViewModel dengan Content (state hoisting).
 * Navigasi diterima sebagai callback (Navigation Hoisting).
 */
@Composable
fun FeedingListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedingListContent(
        state = uiState,
        onBack = onNavigateBack,
        onAdd = onNavigateToAdd,
        onItemClick = onNavigateToDetail,
        onRetry = viewModel::loadSchedules,
        onMessageShown = viewModel::consumeInfoMessage,
        modifier = modifier
    )
}

/** CONTENT: stateless. Daftar jadwal memakai LazyColumn (urut berdasarkan jam makan). */
@Composable
fun FeedingListContent(
    state: FeedingUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== TOP BAR =====
                item { FeedingTopBar(title = "Jadwal Makan", onBack = onBack) }

                // ===== HERO SKY BLUE (FLAT, NO SHADOW) =====
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 22.dp, vertical = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 18.dp, y = (-12).dp)
                                .rotate(-18f)
                        )
                        Column {
                            Text(
                                text = "Jadwal Makan\nHarian",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${state.schedules.size} jadwal tercatat, diurutkan dari jam makan paling pagi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // ===== JUDUL SEKSI + PILL TAMBAH (INK) =====
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daftar Jadwal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ketuk jadwal untuk melihat detail",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FeedingPill(
                            text = "Tambah",
                            icon = Icons.Outlined.Add,
                            background = MaterialTheme.colorScheme.primary,
                            content = MaterialTheme.colorScheme.onPrimary,
                            onClick = onAdd
                        )
                    }
                }

                // ===== KONDISI LOADING / ERROR / KOSONG =====
                if (state.isLoading) {
                    item {
                        Column {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(
                                text = "Mengambil jadwal makan...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = Spacing.sm)
                            )
                        }
                    }
                }

                state.errorMessage?.let { pesan ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            FeedingErrorBanner(message = pesan)
                            Button(onClick = onRetry, shape = CircleShape) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }

                if (state.schedules.isEmpty() && !state.isLoading && state.errorMessage == null) {
                    item {
                        Text(
                            text = "Belum ada jadwal makan. Tekan Tambah untuk membuat jadwal pertama.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ===== DAFTAR KARTU PASTEL FLAT =====
                itemsIndexed(state.schedules, key = { _, jadwal -> jadwal.id }) { index, jadwal ->
                    FeedingCard(
                        schedule = jadwal,
                        useWarmAccent = index % 2 == 1,
                        onClick = { onItemClick(jadwal.id) }
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.md)
            )
        }
    }
}

/** Kartu satu jadwal: jam di kotak putih, lalu nama hewan, makanan, dan porsi. */
@Composable
private fun FeedingCard(
    schedule: FeedingSchedule,
    useWarmAccent: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (useWarmAccent) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val cardContent = if (useWarmAccent) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = schedule.time,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = cardContent
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = schedule.petName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = cardContent
                )
                Text(
                    text = schedule.foodType,
                    style = MaterialTheme.typography.bodySmall,
                    color = cardContent.copy(alpha = 0.9f)
                )
                Text(
                    text = "Porsi: ${schedule.portion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cardContent.copy(alpha = 0.75f)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Lihat detail",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(name = "Feeding List - Light", showBackground = true)
@Preview(name = "Feeding List - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FeedingListPreview() {
    PetCareTheme {
        FeedingListContent(
            state = FeedingUiState(
                schedules = listOf(
                    FeedingSchedule("1", "Mochi", "07:00", "Dry food Royal Canin", "50 gram"),
                    FeedingSchedule("2", "Bruno", "08:00", "Dog food adult", "150 gram")
                )
            ),
            onBack = {}, onAdd = {}, onItemClick = {}, onRetry = {}, onMessageShown = {}
        )
    }
}
