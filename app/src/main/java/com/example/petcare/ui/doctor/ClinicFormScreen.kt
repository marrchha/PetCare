package com.example.petcare.ui.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.ui.theme.PetCareTheme

@Composable
fun ClinicFormScreen(
    clinicId: String?,
    viewModel: DoctorClinicViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val clinicToEdit = remember(clinicId, uiState.clinics) {
        if (!clinicId.isNullOrBlank()) {
            uiState.clinics.find { it.id == clinicId }
        } else {
            null
        }
    }

    ClinicFormContent(
        clinicToEdit = clinicToEdit,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onSave = { name, address, phone, hours, emergency, isActive, description ->
            viewModel.saveClinic(
                id = clinicId,
                name = name,
                address = address,
                phone = phone,
                hours = hours,
                emergency = emergency,
                isActive = isActive,
                description = description,
                onSuccess = onNavigateBack
            )
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicFormContent(
    clinicToEdit: ClinicInfo? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onSave: (
        name: String,
        address: String,
        phone: String,
        hours: String,
        emergency: Boolean,
        isActive: Boolean,
        description: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditing = clinicToEdit != null

    var name by remember { mutableStateOf(clinicToEdit?.name ?: "") }
    var address by remember { mutableStateOf(clinicToEdit?.address ?: "") }
    var phone by remember { mutableStateOf(clinicToEdit?.phoneNumber ?: "") }

    val initialStartHour = remember(clinicToEdit) {
        val raw = clinicToEdit?.operationalHours.orEmpty()
        if (raw.contains("-")) {
            raw.split("-")[0].trim().replace("WIB", "").trim()
        } else {
            "08:00"
        }
    }
    val initialEndHour = remember(clinicToEdit) {
        val raw = clinicToEdit?.operationalHours.orEmpty()
        if (raw.contains("-")) {
            raw.split("-")[1].trim().replace("WIB", "").trim()
        } else {
            "21:00"
        }
    }

    var startHour by remember { mutableStateOf(initialStartHour) }
    var endHour by remember { mutableStateOf(initialEndHour) }
    var emergency by remember { mutableStateOf(clinicToEdit?.emergencyAvailable ?: true) }
    var description by remember { mutableStateOf(clinicToEdit?.description ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val requiredFieldsCount = 5
    val filledCount = listOf(
        name.isNotBlank(),
        address.isNotBlank(),
        phone.isNotBlank(),
        startHour.isNotBlank(),
        endHour.isNotBlank()
    ).count { it }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Data Cabang Klinik" else "Tambah Cabang Klinik Baru",
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
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (name.isBlank() || address.isBlank() || phone.isBlank() || startHour.isBlank() || endHour.isBlank()) {
                            validationError = "Mohon lengkapi seluruh kolom bertanda bintang (*)"
                        } else {
                            validationError = null
                            val combinedHours = "$startHour - $endHour WIB"
                            onSave(
                                name.trim(),
                                address.trim(),
                                phone.trim(),
                                combinedHours,
                                emergency,
                                clinicToEdit?.isActive ?: true,
                                description.trim()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) "Simpan Perubahan Cabang" else "Daftarkan Cabang Klinik",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isEditing) "Informasi Cabang Klinik" else "Pendaftaran Cabang Baru",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Data klinik dikelola langsung oleh Admin Pusat PetCare",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            ClinicFormProgressIndicatorCard(
                filledCount = filledCount,
                totalCount = requiredFieldsCount
            )

            if (validationError != null || errorMessage != null) {
                Text(
                    text = validationError ?: errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Cabang Klinik *") },
                placeholder = { Text("Contoh: Petcare Malang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Alamat Lengkap Cabang *") },
                placeholder = { Text("Contoh: Jl. Brigjend Slamet Riyadi No. 12, Malang") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon / Kontak Darurat *") },
                placeholder = { Text("Contoh: (0341) 555-123 / 0812-3456-7890") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

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
                    placeholder = { Text("21:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Deskripsi & Fasilitas Pendukung") },
                placeholder = { Text("Fasilitas rawat inap, sterilisasi ruang bedah, USG hewan...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Instalasi Gawat Darurat (UGD 24 Jam)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (emergency) "Tersedia 24 Jam Siaga" else "Hanya beroperasi pada jam kerja",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = emergency,
                    onCheckedChange = { emergency = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ClinicFormProgressIndicatorCard(
    filledCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = (filledCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$filledCount dari $totalCount kolom ($percentage%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (filledCount >= totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = if (filledCount >= totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Preview(name = "Clinic Form Screen", showBackground = true)
@Composable
private fun ClinicFormPreview() {
    PetCareTheme {
        ClinicFormContent(
            clinicToEdit = ClinicInfo(
                name = "Petcare Malang",
                address = "Jl. Bendungan Sigura-gura No. 24, Malang",
                phoneNumber = "(0341) 567-890",
                operationalHours = "08:00 - 21:00 WIB",
                emergencyAvailable = true,
                isActive = true,
                description = "Klinik hewan modern dan fasilitas rawat inap."
            )
        )
    }
}
