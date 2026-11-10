package com.example.petcare.ui.doctor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.Doctor
import com.example.petcare.ui.theme.PetCareTheme

@Composable
fun DoctorFormScreen(
    doctorId: String?,
    viewModel: DoctorClinicViewModel,
    onNavigateBack: () -> Unit,
    preselectedClinicId: String? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val doctorToEdit = remember(doctorId, uiState.doctors) {
        if (!doctorId.isNullOrBlank()) {
            uiState.doctors.find { it.id == doctorId }
        } else {
            null
        }
    }

    DoctorFormContent(
        doctorToEdit = doctorToEdit,
        clinics = uiState.clinics,
        defaultClinicId = preselectedClinicId?.takeIf { it.isNotBlank() } ?: uiState.selectedClinicId,
        isClinicLocked = !preselectedClinicId.isNullOrBlank() && doctorToEdit == null,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onSave = { name, specialization, practiceDays, practiceHours, phoneContact, clinicId, clinicName, isActive, exp, notes ->
            viewModel.saveDoctor(
                id = doctorId,
                name = name,
                specialization = specialization,
                practiceDays = practiceDays,
                practiceHours = practiceHours,
                phoneContact = phoneContact,
                clinicId = clinicId,
                clinicName = clinicName,
                photoUri = null,
                avatarIndex = 1,
                isActive = isActive,
                experienceYears = exp,
                notes = notes,
                onSuccess = onNavigateBack
            )
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DoctorFormContent(
    doctorToEdit: Doctor? = null,
    clinics: List<ClinicInfo> = emptyList(),
    defaultClinicId: String = "",
    isClinicLocked: Boolean = false,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSave: (
        name: String,
        specialization: String,
        practiceDays: String,
        practiceHours: String,
        phoneContact: String,
        clinicId: String,
        clinicName: String,
        isActive: Boolean,
        experienceYears: Int,
        notes: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditing = doctorToEdit != null

    val initialHoursSplit = remember(doctorToEdit) {
        val raw = doctorToEdit?.practiceHours ?: "08:00 - 15:00"
        val parts = raw.split("-").map { it.trim() }
        val start = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "08:00"
        val end = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "15:00"
        start to end
    }

    var name by remember { mutableStateOf(doctorToEdit?.name ?: "") }
    var specialization by remember { mutableStateOf(doctorToEdit?.specialization ?: "") }
    var practiceDays by remember { mutableStateOf(doctorToEdit?.practiceDays ?: "Senin, Rabu, Jumat") }
    var startHour by remember { mutableStateOf(initialHoursSplit.first) }
    var endHour by remember { mutableStateOf(initialHoursSplit.second) }
    var phoneContact by remember { mutableStateOf(doctorToEdit?.phoneContact ?: "") }
    var experienceYearsText by remember { mutableStateOf(doctorToEdit?.experienceYears?.toString() ?: "3") }
    var isActive by remember { mutableStateOf(doctorToEdit?.isActive ?: true) }
    var notes by remember { mutableStateOf(doctorToEdit?.notes ?: "") }

    var selectedClinicId by remember {
        mutableStateOf(
            doctorToEdit?.clinicId
                ?: defaultClinicId.ifBlank { clinics.firstOrNull()?.id ?: "" }
        )
    }
    var clinicDropdownExpanded by remember { mutableStateOf(false) }

    val currentClinic = clinics.find { it.id == selectedClinicId } ?: clinics.firstOrNull()

    // Progress Form Calculation (6 required fields)
    val totalRequiredFields = 6
    val filledRequiredFields = listOf(
        name.isNotBlank(),
        specialization.isNotBlank(),
        (currentClinic != null),
        phoneContact.isNotBlank(),
        practiceDays.isNotBlank(),
        (startHour.isNotBlank() && endHour.isNotBlank())
    ).count { it }
    val progressFraction = filledRequiredFields.toFloat() / totalRequiredFields.toFloat()

    val specializationOptions = listOf("Dokter Umum", "Bedah", "Kulit", "Gigi")
    val allDays = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Profil Dokter" else "Tambah Dokter Baru",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card Pendaftaran Tenaga Medis (Tanpa Foto)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isEditing) "Perbarui Data Tenaga Medis" else "Pendaftaran Tenaga Medis",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (isClinicLocked && currentClinic != null) {
                                "Menambahkan dokter langsung untuk cabang ${currentClinic.name}"
                            } else {
                                "Data dokter akan langsung terdaftar pada klinik penugasan"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Progress Indicator — tulisan "Form Filled" di ATAS garis progress
            DoctorFormProgressIndicatorCard(
                filledCount = filledRequiredFields,
                totalCount = totalRequiredFields,
                progress = progressFraction
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap Dokter *") },
                placeholder = { Text("Contoh: drh. Andi Prasetya") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Spesialisasi *") },
                placeholder = { Text("Pilih atau ketik spesialisasi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Quick chips spesialisasi
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                specializationOptions.forEach { option ->
                    val selected = specialization.equals(option, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { specialization = option },
                        label = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Pilihan Klinik Penugasan
            if (clinics.isNotEmpty()) {
                if (isClinicLocked && currentClinic != null) {
                    OutlinedTextField(
                        value = currentClinic.name,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Klinik Penugasan (Otomatis) *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocalHospital,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = clinicDropdownExpanded,
                        onExpandedChange = { clinicDropdownExpanded = !clinicDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentClinic?.name ?: "Pilih Klinik",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Klinik Penugasan *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.LocalHospital,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = clinicDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(14.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = clinicDropdownExpanded,
                            onDismissRequest = { clinicDropdownExpanded = false }
                        ) {
                            clinics.forEach { clinic ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(clinic.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                clinic.address,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedClinicId = clinic.id
                                        clinicDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = phoneContact,
                onValueChange = { phoneContact = it },
                label = { Text("Nomor Kontak / WhatsApp *") },
                placeholder = { Text("0812-3456-0001") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = practiceDays,
                onValueChange = { practiceDays = it },
                label = { Text("Hari Praktik *") },
                placeholder = { Text("Senin, Rabu, Jumat") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Quick toggle chips untuk Hari Praktik
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val selectedDaysSet = practiceDays
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                allDays.forEach { day ->
                    val isDaySelected = selectedDaysSet.any { it.equals(day, ignoreCase = true) }
                    FilterChip(
                        selected = isDaySelected,
                        onClick = {
                            val updated = if (isDaySelected) {
                                selectedDaysSet.filterNot { it.equals(day, ignoreCase = true) }
                            } else {
                                selectedDaysSet + day
                            }
                            practiceDays = allDays.filter { d ->
                                updated.any { it.equals(d, ignoreCase = true) }
                            }.joinToString(", ")
                        },
                        label = {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }

            // Jam Mulai & Jam Selesai Praktik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = startHour,
                    onValueChange = { startHour = it },
                    label = { Text("Jam Mulai *") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = endHour,
                    onValueChange = { endHour = it },
                    label = { Text("Jam Selesai *") },
                    placeholder = { Text("15:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            OutlinedTextField(
                value = experienceYearsText,
                onValueChange = { experienceYearsText = it.filter { c -> c.isDigit() } },
                label = { Text("Pengalaman Praktik (Tahun)") },
                placeholder = { Text("4") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Khusus") },
                placeholder = { Text("Keahlian khusus, catatan pasien, atau jadwal cuti...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Status Praktik Aktif
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status Praktik Aktif",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isActive) "Dokter aktif bertugas menerima pasien" else "Dokter sedang libur / nonaktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    val exp = experienceYearsText.toIntOrNull() ?: 0
                    val clinic = currentClinic
                    val combinedHours = "${startHour.trim()} - ${endHour.trim()}"
                    onSave(
                        name,
                        specialization,
                        practiceDays,
                        combinedHours,
                        phoneContact,
                        clinic?.id ?: "",
                        clinic?.name ?: "Petcare Malang",
                        isActive,
                        exp,
                        notes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Simpan Perubahan" else "Simpan Data Dokter",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Komponen Indikator Progress Form Dokter:
 * Tulisan "Form Filled" diletakkan di ATAS garis progress indicator,
 * rapi dan tidak bertabrakan dengan garis/indikator.
 */
@Composable
fun DoctorFormProgressIndicatorCard(
    filledCount: Int,
    totalCount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (progress.coerceIn(0f, 1f) * 100).toInt()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Form Filled",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$filledCount dari $totalCount kolom ($percentage%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Preview(name = "Doctor Form", showBackground = true)
@Preview(
    name = "Doctor Form - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun DoctorFormPreview() {
    PetCareTheme {
        DoctorFormContent(
            clinics = listOf(
                ClinicInfo(id = "c1", name = "Petcare Malang", address = "Malang", phoneNumber = "0341"),
                ClinicInfo(id = "c2", name = "Petcare Batu", address = "Batu", phoneNumber = "0341")
            ),
            defaultClinicId = "c2"
        )
    }
}
