package com.example.petcare.data.repository

import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.ClinicService
import com.example.petcare.data.model.Doctor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository untuk Fitur 2: Manajemen Dokter Hewan & Klinik.
 * Mengelola operasi in-memory / mock CRUD untuk Progress 1:
 * - Data Klinik lebih dari 1 (multi-klinik/cabang)
 * - Data Dokter dengan foto profil & penempatan klinik
 * - Data Layanan klinik
 */
class DoctorClinicRepository {

    companion object {
        // Multi-Klinik (Lebih dari 1 Klinik PetCare)
        private val initialClinics = mutableListOf(
            ClinicInfo(
                id = "clinic-001",
                name = "PetCare Central Vet & Hospital",
                address = "Jl. Bendungan Sigura-gura No. 24, Sukun, Kota Malang",
                phoneNumber = "(0341) 567-890",
                operationalHours = "Senin - Minggu: 08:00 - 21:00 WIB",
                emergencyAvailable = true,
                description = "Pusat pelayanan utama terpadu dengan fasilitas rawat inap, lab darah, dan bedah mayor."
            ),
            ClinicInfo(
                id = "clinic-002",
                name = "PetCare Cabang Soekarno-Hatta",
                address = "Jl. Soekarno-Hatta Kav. 88, Lowokwaru, Kota Malang",
                phoneNumber = "(0341) 489-112",
                operationalHours = "Senin - Sabtu: 09:00 - 20:00 WIB",
                emergencyAvailable = false,
                description = "Klinik cabang modern untuk vaksinasi, sterilisasi, grooming medis, dan konsultasi rutin."
            ),
            ClinicInfo(
                id = "clinic-003",
                name = "PetCare Emergency Sawojajar",
                address = "Jl. Danau Toba Raya Blok E-15, Sawojajar, Kota Malang",
                phoneNumber = "(0341) 712-334",
                operationalHours = "Buka 24 Jam Setiap Hari",
                emergencyAvailable = true,
                description = "Unit gawat darurat & observasi intensif 24 jam untuk kondisi darurat hewan peliharaan."
            )
        )

        // Mock Data Dokter Hewan dengan Foto & Penempatan Klinik
        private val initialDoctors = mutableListOf(
            Doctor(
                id = "doc-001",
                name = "drh. Sarah Wijayanti, M.Si",
                specialization = "Bedah & Penyakit Dalam Kucing",
                practiceDays = "Senin - Kamis",
                practiceHours = "09:00 - 15:00",
                phoneContact = "0812-3456-7891",
                clinicId = "clinic-001",
                clinicName = "PetCare Central Vet & Hospital",
                avatarIndex = 1,
                isActive = true,
                experienceYears = 7,
                notes = "Fokus pada sterilisasi dan penanganan infeksi saluran cerna."
            ),
            Doctor(
                id = "doc-002",
                name = "drh. Raditya Pratama",
                specialization = "Spesialis Gigi & Bedah Tulang",
                practiceDays = "Senin - Sabtu",
                practiceHours = "13:00 - 19:00",
                phoneContact = "0813-8822-1100",
                clinicId = "clinic-002",
                clinicName = "PetCare Cabang Soekarno-Hatta",
                avatarIndex = 2,
                isActive = true,
                experienceYears = 5,
                notes = "Ahli ortopedi dan scaling karang gigi hewan kecil."
            ),
            Doctor(
                id = "doc-003",
                name = "drh. Amanda Lestari, Sp.KGH",
                specialization = "Dermatologi & Perawatan Kulit/Bulu",
                practiceDays = "Rabu - Minggu",
                practiceHours = "10:00 - 17:00",
                phoneContact = "0817-4567-9922",
                clinicId = "clinic-003",
                clinicName = "PetCare Emergency Sawojajar",
                avatarIndex = 3,
                isActive = true,
                experienceYears = 9,
                notes = "Menangani alergi kronis, kutu, dan infeksi jamur."
            ),
            Doctor(
                id = "doc-004",
                name = "drh. Budi Wicaksono",
                specialization = "Pemeriksaan Umum & Vaksinasi",
                practiceDays = "Jumat - Minggu",
                practiceHours = "08:30 - 14:00",
                phoneContact = "0821-7788-3344",
                clinicId = "clinic-001",
                clinicName = "PetCare Central Vet & Hospital",
                avatarIndex = 4,
                isActive = false,
                experienceYears = 3,
                notes = "Sedang cuti studi lanjut pascasarjana."
            )
        )

        // Mock Data Layanan Klinik
        private val initialServices = mutableListOf(
            ClinicService(
                id = "srv-001",
                name = "Vaksinasi Lengkap (F3 / F4)",
                description = "Pemberian vaksin inti untuk pencegahan panleukopenia, calici, dan rhinotracheitis.",
                estimatedPrice = 180000,
                durationMinutes = 20,
                category = "Pencegahan"
            ),
            ClinicService(
                id = "srv-002",
                name = "Konsultasi & Pemeriksaan Rutin",
                description = "Pemeriksaan fisik menyeluruh meliputi suhu badan, berat, telinga, mata, dan detak jantung.",
                estimatedPrice = 75000,
                durationMinutes = 15,
                category = "Pemeriksaan"
            ),
            ClinicService(
                id = "srv-003",
                name = "Sterilisasi Kucing Jantan & Betina",
                description = "Tindakan bedah kebiri steril aman dengan pembiusan inhalasi dan observasi pasca-operasi.",
                estimatedPrice = 450000,
                durationMinutes = 60,
                category = "Tindakan Bedah"
            ),
            ClinicService(
                id = "srv-004",
                name = "Dental Scaling & Polishing",
                description = "Pembersihan karang gigi ultrasonik untuk mencegah gingivitis dan bau mulut.",
                estimatedPrice = 250000,
                durationMinutes = 40,
                category = "Perawatan"
            ),
            ClinicService(
                id = "srv-005",
                name = "Rawat Inap Intensif (Per Hari)",
                description = "Ruang isolasi ber-AC dilengkapi infus monitor dan pengawasan dokter 24 jam.",
                estimatedPrice = 120000,
                durationMinutes = 1440,
                category = "Rawat Inap"
            )
        )

        private val _clinicsFlow = MutableStateFlow<List<ClinicInfo>>(initialClinics.toList())
        private val _selectedClinicIdFlow = MutableStateFlow<String>(initialClinics.first().id)
        private val _doctorsFlow = MutableStateFlow<List<Doctor>>(initialDoctors.toList())
        private val _servicesFlow = MutableStateFlow<List<ClinicService>>(initialServices.toList())
    }

    val clinicsFlow: StateFlow<List<ClinicInfo>> = _clinicsFlow.asStateFlow()
    val selectedClinicIdFlow: StateFlow<String> = _selectedClinicIdFlow.asStateFlow()
    val doctorsFlow: StateFlow<List<Doctor>> = _doctorsFlow.asStateFlow()
    val servicesFlow: StateFlow<List<ClinicService>> = _servicesFlow.asStateFlow()

    // ==========================================
    // OPERASI DOKTER HEWAN (CRUD)
    // ==========================================

    suspend fun getDoctorById(id: String): Doctor? {
        delay(100)
        return _doctorsFlow.value.find { it.id == id }
    }

    suspend fun addDoctor(doctor: Doctor): Result<Doctor> {
        delay(200)
        return try {
            val newDoctor = if (doctor.id.isBlank()) doctor.copy(id = UUID.randomUUID().toString()) else doctor
            initialDoctors.add(0, newDoctor)
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(newDoctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctor: Doctor): Result<Doctor> {
        delay(200)
        val index = initialDoctors.indexOfFirst { it.id == doctor.id }
        return if (index != -1) {
            initialDoctors[index] = doctor
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(doctor)
        } else {
            Result.failure(IllegalArgumentException("Data dokter tidak ditemukan."))
        }
    }

    suspend fun toggleDoctorStatus(id: String): Result<Doctor> {
        delay(150)
        val index = initialDoctors.indexOfFirst { it.id == id }
        return if (index != -1) {
            val current = initialDoctors[index]
            val updated = current.copy(isActive = !current.isActive)
            initialDoctors[index] = updated
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(updated)
        } else {
            Result.failure(IllegalArgumentException("Data dokter tidak ditemukan."))
        }
    }

    suspend fun deleteDoctor(id: String): Result<Unit> {
        delay(200)
        val removed = initialDoctors.removeAll { it.id == id }
        return if (removed) {
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Dokter tidak dapat dihapus."))
        }
    }

    // ==========================================
    // OPERASI LAYANAN KLINIK (CRUD)
    // ==========================================

    suspend fun addService(service: ClinicService): Result<ClinicService> {
        delay(200)
        return try {
            val newService = if (service.id.isBlank()) service.copy(id = UUID.randomUUID().toString()) else service
            initialServices.add(0, newService)
            _servicesFlow.value = initialServices.toList()
            Result.success(newService)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateService(service: ClinicService): Result<ClinicService> {
        delay(200)
        val index = initialServices.indexOfFirst { it.id == service.id }
        return if (index != -1) {
            initialServices[index] = service
            _servicesFlow.value = initialServices.toList()
            Result.success(service)
        } else {
            Result.failure(IllegalArgumentException("Layanan tidak ditemukan."))
        }
    }

    suspend fun deleteService(id: String): Result<Unit> {
        delay(200)
        val removed = initialServices.removeAll { it.id == id }
        return if (removed) {
            _servicesFlow.value = initialServices.toList()
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Layanan tidak ditemukan."))
        }
    }

    // ==========================================
    // OPERASI MULTI-KLINIK (CRUD)
    // ==========================================

    fun selectClinic(clinicId: String) {
        _selectedClinicIdFlow.value = clinicId
    }

    suspend fun addClinic(clinic: ClinicInfo): Result<ClinicInfo> {
        delay(200)
        return try {
            val newClinic = if (clinic.id.isBlank()) clinic.copy(id = UUID.randomUUID().toString()) else clinic
            initialClinics.add(newClinic)
            _clinicsFlow.value = initialClinics.toList()
            _selectedClinicIdFlow.value = newClinic.id
            Result.success(newClinic)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClinicInfo(info: ClinicInfo): Result<ClinicInfo> {
        delay(200)
        val index = initialClinics.indexOfFirst { it.id == info.id }
        return if (index != -1) {
            initialClinics[index] = info
            _clinicsFlow.value = initialClinics.toList()
            Result.success(info)
        } else {
            Result.failure(IllegalArgumentException("Klinik tidak ditemukan."))
        }
    }

    suspend fun deleteClinic(clinicId: String): Result<Unit> {
        delay(200)
        if (initialClinics.size <= 1) {
            return Result.failure(IllegalStateException("Minimal harus tersisa 1 klinik aktif."))
        }
        val removed = initialClinics.removeAll { it.id == clinicId }
        return if (removed) {
            _clinicsFlow.value = initialClinics.toList()
            if (_selectedClinicIdFlow.value == clinicId) {
                _selectedClinicIdFlow.value = initialClinics.first().id
            }
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Klinik tidak ditemukan."))
        }
    }
}
