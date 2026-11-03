package com.example.petcare.ui.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.ClinicService
import com.example.petcare.ui.theme.PetCareTheme


@Composable
fun ServiceFormDialog(
    service: ClinicService? = null,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, category: String, price: Long, duration: Int, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var category by remember { mutableStateOf(service?.category ?: "Pemeriksaan") }
    var priceText by remember { mutableStateOf(service?.estimatedPrice?.toString() ?: "100000") }
    var durationText by remember { mutableStateOf(service?.durationMinutes?.toString() ?: "30") }
    var description by remember { mutableStateOf(service?.description ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (service == null) "Tambah Layanan Klinik" else "Ubah Layanan Klinik",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Layanan") },
                    placeholder = { Text("Contoh: Vaksinasi Kucing Tricat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori Layanan") },
                    placeholder = { Text("Misal: Bedah, Perawatan, Lab") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { char -> char.isDigit() } },
                    label = { Text("Estimasi Biaya (Rp)") },
                    placeholder = { Text("150000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { char -> char.isDigit() } },
                    label = { Text("Durasi Tindakan (Menit)") },
                    placeholder = { Text("30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan / Prosedur") },
                    placeholder = { Text("Deskripsi singkat tentang layanan...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isError) {
                    Text(
                        text = "Mohon lengkapi nama layanan dan biaya yang valid.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.toLongOrNull() ?: 0L
                    val parsedDuration = durationText.toIntOrNull() ?: 30
                    if (name.isBlank() || parsedPrice <= 0) {
                        isError = true
                    } else {
                        onSave(
                            service?.id,
                            name,
                            category,
                            parsedPrice,
                            parsedDuration,
                            description
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}


@Composable
fun ClinicInfoEditDialog(
    clinicInfo: ClinicInfo? = null,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, address: String, phone: String, hours: String, emergency: Boolean, desc: String) -> Unit
) {
    val isEditing = clinicInfo != null

    var name by remember { mutableStateOf(clinicInfo?.name ?: "") }
    var address by remember { mutableStateOf(clinicInfo?.address ?: "") }
    var phone by remember { mutableStateOf(clinicInfo?.phoneNumber ?: "") }
    var hours by remember { mutableStateOf(clinicInfo?.operationalHours ?: "08:00 - 20:00 WIB") }
    var emergency by remember { mutableStateOf(clinicInfo?.emergencyAvailable ?: true) }
    var desc by remember { mutableStateOf(clinicInfo?.description ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Ubah Informasi Klinik" else "Tambah Cabang Klinik Baru",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Klinik / Cabang") },
                    placeholder = { Text("Contoh: PetCare Cabang Soekarno-Hatta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Lengkap") },
                    placeholder = { Text("Jl. Nama Jalan No. XX, Kota") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Telepon / Kontak") },
                    placeholder = { Text("(0341) 123-456") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Jam Operasional") },
                    placeholder = { Text("08:00 - 21:00 WIB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi & Fasilitas") },
                    placeholder = { Text("Fasilitas klinik, ruang tunggu, dll...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Layanan Gawat Darurat (UGD 24 Jam)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (emergency) "Tersedia 24 Jam" else "Hanya pada jam operasional",
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

                if (isError) {
                    Text(
                        text = "Nama klinik, alamat, dan nomor telepon tidak boleh kosong.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                        isError = true
                    } else {
                        onSave(clinicInfo?.id, name, address, phone, hours, emergency, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}


@Preview(name = "Service Form Dialog", showBackground = true)
@Composable
private fun ServiceFormDialogPreview() {
    PetCareTheme {
        ServiceFormDialog(
            service = ClinicService(
                name = "Vaksinasi Kucing F3",
                description = "Vaksin perlindungan dasar dari feline panleukopenia.",
                estimatedPrice = 180000,
                durationMinutes = 20,
                category = "Pencegahan"
            ),
            onDismiss = {},
            onSave = { _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Clinic Info Dialog", showBackground = true)
@Composable
private fun ClinicInfoEditDialogPreview() {
    PetCareTheme {
        ClinicInfoEditDialog(
            clinicInfo = ClinicInfo(
                name = "PetCare Central Clinic",
                address = "Jl. Bendungan Sigura-gura No. 24, Malang",
                phoneNumber = "(0341) 567-890",
                operationalHours = "08:00 - 21:00 WIB",
                emergencyAvailable = true,
                description = "Klinik hewan modern dan fasilitas rawat inap."
            ),
            onDismiss = {},
            onSave = { _, _, _, _, _, _, _ -> }
        )
    }
}
