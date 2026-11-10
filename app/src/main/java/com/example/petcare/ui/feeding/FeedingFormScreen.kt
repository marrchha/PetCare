package com.example.petcare.ui.feeding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.Spacing

/**
 * SCREEN: form Tambah/Ubah Jadwal Makan.
 */
@Composable
fun FeedingFormScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
            viewModel.consumeSaved()
        }
    }

    FeedingFormContent(
        state = uiState,
        pet = viewModel.formPet,
        time = viewModel.formTime,
        food = viewModel.formFood,
        portion = viewModel.formPortion,
        notes = viewModel.formNotes,
        onPetChange = viewModel::onPetChange,
        onTimeChange = viewModel::onTimeChange,
        onFoodChange = viewModel::onFoodChange,
        onPortionChange = viewModel::onPortionChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::saveSchedule,
        onBack = onNavigateBack,
        modifier = modifier
    )
}

/** CONTENT: stateless. */
@Composable
fun FeedingFormContent(
    state: FeedingUiState,
    pet: String,
    time: String,
    food: String,
    portion: String,
    notes: String,
    onPetChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onFoodChange: (String) -> Unit,
    onPortionChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = state.editingId != null

    val flatInputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = Spacing.md)
        ) {
            FeedingTopBar(
                title = if (isEdit) "Ubah Jadwal" else "Tambah Jadwal",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = if (isEdit) "Ubah Jadwal Makan" else "Jadwal Makan Baru",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Isi jam makan, jenis makanan, dan porsi hewanmu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // ===== PESAN ERROR FLAT =====
            AnimatedVisibility(visible = state.formError != null) {
                state.formError?.let {
                    FeedingErrorBanner(
                        message = it,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
            }

            FormField(
                label = "Nama Hewan",
                value = pet,
                onValueChange = onPetChange,
                placeholder = "Contoh: Mochi",
                icon = Icons.Outlined.Pets,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                colors = flatInputColors
            )
            Spacer(modifier = Modifier.height(14.dp))
            FormField(
                label = "Jam Makan (HH:mm)",
                value = time,
                onValueChange = onTimeChange,
                placeholder = "07:30",
                icon = Icons.Outlined.Schedule,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = flatInputColors
            )
            Spacer(modifier = Modifier.height(14.dp))
            FormField(
                label = "Jenis Makanan",
                value = food,
                onValueChange = onFoodChange,
                placeholder = "Contoh: Dry food Royal Canin",
                icon = Icons.Outlined.Restaurant,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = flatInputColors
            )
            Spacer(modifier = Modifier.height(14.dp))
            FormField(
                label = "Porsi",
                value = portion,
                onValueChange = onPortionChange,
                placeholder = "Contoh: 50 gram",
                icon = Icons.Outlined.Scale,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = flatInputColors
            )
            Spacer(modifier = Modifier.height(14.dp))
            FormField(
                label = "Catatan (opsional)",
                value = notes,
                onValueChange = onNotesChange,
                placeholder = "Contoh: campur sedikit air hangat",
                icon = Icons.Outlined.Notes,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = flatInputColors,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isEdit) "Simpan Perubahan" else "Simpan Jadwal",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(enabled = !state.isSaving, onClick = onBack)
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Batal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardOptions: KeyboardOptions,
    colors: androidx.compose.material3.TextFieldColors,
    singleLine: Boolean = true
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp, start = Spacing.xs)
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboardOptions,
        colors = colors,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(name = "Feeding Form - Light", showBackground = true)
@Preview(name = "Feeding Form - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FeedingFormPreview() {
    PetCareTheme {
        FeedingFormContent(
            state = FeedingUiState(),
            pet = "Mochi", time = "07:00", food = "Dry food", portion = "50 gram", notes = "",
            onPetChange = {}, onTimeChange = {}, onFoodChange = {}, onPortionChange = {},
            onNotesChange = {}, onSave = {}, onBack = {}
        )
    }
}
