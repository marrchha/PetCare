package com.example.petcare.ui.doctor

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.Doctor
import com.example.petcare.ui.theme.PetCareTheme

/**
 * Stateful Container untuk Form Tambah / Edit Dokter Hewan.
 */
@Composable
fun DoctorFormScreen(
    doctorId: String?,
    viewModel: DoctorClinicViewModel,
    onNavigateBack: () -> Unit,
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
        defaultClinicId = uiState.selectedClinicId,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onSave = { name, specialization, practiceDays, practiceHours, phoneContact, clinicId, clinicName, photoUri, avatarIndex, isActive, exp, notes ->
            viewModel.saveDoctor(
                id = doctorId,
                name = name,
                specialization = specialization,
                practiceDays = practiceDays,
                practiceHours = practiceHours,
                phoneContact = phoneContact,
                clinicId = clinicId,
                clinicName = clinicName,
                photoUri = photoUri,
                avatarIndex = avatarIndex,
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

/**
 * Stateless UI Content Form Tambah / Ubah Dokter:
 * - Dilengkapi pemilih Foto Galeri & Avatar Profil Dokter
 * - Dilengkapi pemilihan Cabang Klinik tempat bertugas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorFormContent(
    doctorToEdit: Doctor? = null,
    clinics: List<ClinicInfo> = emptyList(),
    defaultClinicId: String = "",
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
        photoUri: String?,
        avatarIndex: Int,
        isActive: Boolean,
        experienceYears: Int,
        notes: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditing = doctorToEdit != null

    var name by remember { mutableStateOf(doctorToEdit?.name ?: "") }
    var specialization by remember { mutableStateOf(doctorToEdit?.specialization ?: "") }
    var practiceDays by remember { mutableStateOf(doctorToEdit?.practiceDays ?: "Senin - Kamis") }
    var practiceHours by remember { mutableStateOf(doctorToEdit?.practiceHours ?: "09:00 - 15:00") }
    var phoneContact by remember { mutableStateOf(doctorToEdit?.phoneContact ?: "") }
    var experienceYearsText by remember { mutableStateOf(doctorToEdit?.experienceYears?.toString() ?: "3") }
    var isActive by remember { mutableStateOf(doctorToEdit?.isActive ?: true) }
    var notes by remember { mutableStateOf(doctorToEdit?.notes ?: "") }

    // Foto + avatar
    var photoUri by remember { mutableStateOf(doctorToEdit?.photoUri) }
    var avatarIndex by remember { mutableIntStateOf(doctorToEdit?.avatarIndex ?: 1) }

    // Klinik
    var selectedClinicId by remember {
        mutableStateOf(doctorToEdit?.clinicId ?: defaultClinicId.ifBlank { clinics.firstOrNull()?.id ?: "" })
    }
    var clinicDropdownExpanded by remember { mutableStateOf(false) }

    val currentClinic = clinics.find { it.id == selectedClinicId } ?: clinics.firstOrNull()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Ubah Data Dokter" else "Tambah Dokter Baru",
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
            //foto dokter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Foto Profil Dokter",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    DoctorPhotoThumbnail(
                        photoUri = photoUri,
                        avatarIndex = avatarIndex,
                        size = 84.dp
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = "Pilih Foto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Pilih dari Galeri", style = MaterialTheme.typography.labelSmall)
                    }

                    if (photoUri != null) {
                        Button(
                            onClick = { photoUri = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Reset", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Atau pilih avatar default:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Pilihan Avatar Presets 1..4
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1, 2, 3, 4).forEach { index ->
                        val isSelected = avatarIndex == index && photoUri == null
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    avatarIndex = index
                                    photoUri = null
                                }
                        ) {
                            DoctorPhotoThumbnail(
                                photoUri = null,
                                avatarIndex = index,
                                size = 42.dp
                            )
                        }
                    }
                }
            }

            // error
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            //input nama n pesialis
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap Dokter & Gelar") },
                placeholder = { Text("drh. Sarah Wijaya, M.Si") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Spesialisasi") },
                placeholder = { Text("Bedah & Penyakit Dalam") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            //multi klinik
            if (clinics.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = clinicDropdownExpanded,
                    onExpandedChange = { clinicDropdownExpanded = !clinicDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentClinic?.name ?: "Pilih Klinik",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cabang Klinik Praktik") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clinicDropdownExpanded) },
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

            //jadwal n kontak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = practiceDays,
                    onValueChange = { practiceDays = it },
                    label = { Text("Hari Praktik") },
                    placeholder = { Text("Senin - Kamis") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = practiceHours,
                    onValueChange = { practiceHours = it },
                    label = { Text("Jam Praktik") },
                    placeholder = { Text("09:00 - 15:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            OutlinedTextField(
                value = phoneContact,
                onValueChange = { phoneContact = it },
                label = { Text("Nomor Kontak / WhatsApp") },
                placeholder = { Text("081234567890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = experienceYearsText,
                onValueChange = { experienceYearsText = it.filter { c -> c.isDigit() } },
                label = { Text("Pengalaman Praktik (Tahun)") },
                placeholder = { Text("5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / Keterangan Khusus") },
                placeholder = { Text("Keahlian khusus, catatan pasien, atau jadwal cuti...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // status doketre Aktif Praktik
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Status Praktik Aktif",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isActive) "Dokter aktif bertugas menerima pasien" else "Dokter sedang libur / cuti",
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

            Spacer(modifier = Modifier.height(10.dp))

            // tombol simpan
            Button(
                onClick = {
                    val exp = experienceYearsText.toIntOrNull() ?: 0
                    val clinic = currentClinic
                    onSave(
                        name,
                        specialization,
                        practiceDays,
                        practiceHours,
                        phoneContact,
                        clinic?.id ?: "",
                        clinic?.name ?: "PetCare Clinic",
                        photoUri,
                        avatarIndex,
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
                        text = if (isEditing) "Simpan Perubahan" else "Tambahkan Dokter",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Doctor Form with Photo Picker", showBackground = true)
@Preview(
    name = "Doctor Form - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun DoctorFormPreview() {
    PetCareTheme {
        DoctorFormContent(
            doctorToEdit = Doctor(
                name = "drh. Raditya Pratama",
                specialization = "Gigi & Ortopedi",
                practiceDays = "Senin - Sabtu",
                practiceHours = "13:00 - 19:00",
                phoneContact = "0813-8822-1100",
                clinicName = "PetCare Cabang Soekarno-Hatta",
                avatarIndex = 2,
                isActive = true,
                experienceYears = 5
            ),
            clinics = listOf(
                ClinicInfo(name = "PetCare Central Vet", address = "Malang", phoneNumber = "0341"),
                ClinicInfo(name = "PetCare Cabang Suhat", address = "Lowokwaru", phoneNumber = "0341")
            )
        )
    }
}
