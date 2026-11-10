package com.example.petcare.data.repository

import com.example.petcare.data.model.ClinicInfo
import com.example.petcare.data.model.ClinicService
import com.example.petcare.data.model.Doctor
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class DoctorClinicRepository {

    companion object {
        private const val TABLE_CLINICS = "klinik_info"
        private const val TABLE_DOCTORS = "dokter"
        private const val TABLE_SERVICES = "layanan_klinik"

        private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var hasFetchedInitialRemote = false

        private val initialClinics = mutableListOf(
            ClinicInfo(
                id = "clinic-001",
                name = "Petcare Malang",
                address = "Jl. Bendungan Sigura-gura No. 24, Sukun, Kota Malang",
                phoneNumber = "(0341) 567-890",
                operationalHours = "Senin - Minggu: 08:00 - 21:00 WIB",
                emergencyAvailable = true,
                isActive = true,
                description = "Pusat pelayanan utama terpadu dengan fasilitas rawat inap, lab darah, dan bedah mayor."
            ),
            ClinicInfo(
                id = "clinic-002",
                name = "Petcare Surabaya",
                address = "Jl. Raya Darmo No. 45, Wonokromo, Kota Surabaya",
                phoneNumber = "(031) 489-112",
                operationalHours = "Senin - Minggu: 08:00 - 22:00 WIB",
                emergencyAvailable = true,
                isActive = true,
                description = "Klinik cabang modern untuk vaksinasi, sterilisasi, grooming medis, dan konsultasi rutin."
            ),
            ClinicInfo(
                id = "clinic-003",
                name = "Petcare Batu",
                address = "Jl. Panglima Sudirman No. 12, Kota Batu",
                phoneNumber = "(0341) 712-334",
                operationalHours = "Senin - Sabtu: 09:00 - 18:00 WIB",
                emergencyAvailable = false,
                isActive = false,
                description = "Cabang layanan pemeriksaan umum, perawatan kulit, dan dental hewan kecil."
            )
        )

        private val initialDoctors = mutableListOf(
            Doctor(
                id = "doc-001",
                name = "drh. Andi Prasetya",
                specialization = "Dokter Umum",
                practiceDays = "Senin, Selasa, Rabu, Kamis",
                practiceHours = "08:00 - 14:00",
                phoneContact = "0812-3456-0001",
                clinicId = "clinic-001",
                clinicName = "Petcare Malang",
                isActive = true,
                experienceYears = 4,
                notes = "Menangani pemeriksaan fisik menyeluruh, konsultasi nutrisi, dan vaksinasi rutin."
            ),
            Doctor(
                id = "doc-002",
                name = "drh. Sarah Wijayanti",
                specialization = "Bedah",
                practiceDays = "Senin, Rabu, Jumat",
                practiceHours = "10:00 - 16:00",
                phoneContact = "0812-3456-7891",
                clinicId = "clinic-001",
                clinicName = "Petcare Malang",
                isActive = true,
                experienceYears = 7,
                notes = "Fokus pada sterilisasi, operasi jaringan lunak, dan penanganan pasca-trauma."
            ),
            Doctor(
                id = "doc-003",
                name = "drh. Hendra Kusuma",
                specialization = "Gigi",
                practiceDays = "Selasa, Kamis, Sabtu",
                practiceHours = "13:00 - 19:00",
                phoneContact = "0813-5566-7788",
                clinicId = "clinic-001",
                clinicName = "Petcare Malang",
                isActive = false,
                experienceYears = 5,
                notes = "Spesialis dental scaling ultrasonik dan ekstraksi gigi hewan."
            ),
            Doctor(
                id = "doc-004",
                name = "drh. Budi Santoso",
                specialization = "Kulit",
                practiceDays = "Senin, Rabu, Jumat, Sabtu",
                practiceHours = "09:00 - 15:00",
                phoneContact = "0821-7788-3344",
                clinicId = "clinic-002",
                clinicName = "Petcare Surabaya",
                isActive = true,
                experienceYears = 6,
                notes = "Menangani alergi kulit kronis, ektoparasit, dan infeksi jamur."
            ),
            Doctor(
                id = "doc-005",
                name = "drh. Amanda Lestari",
                specialization = "Bedah",
                practiceDays = "Rabu, Kamis, Jumat, Minggu",
                practiceHours = "10:00 - 17:00",
                phoneContact = "0817-4567-9922",
                clinicId = "clinic-002",
                clinicName = "Petcare Surabaya",
                isActive = true,
                experienceYears = 9,
                notes = "Ahli bedah ortopedi dan tindakan darurat."
            ),
            Doctor(
                id = "doc-006",
                name = "drh. Citra Maharani",
                specialization = "Dokter Umum",
                practiceDays = "Senin, Selasa, Jumat",
                practiceHours = "09:00 - 15:00",
                phoneContact = "0813-8822-1100",
                clinicId = "clinic-003",
                clinicName = "Petcare Batu",
                isActive = true,
                experienceYears = 3,
                notes = "Pemeriksaan rutin, pemberian obat cacing, dan konsultasi perawatan anak hewan."
            ),
            Doctor(
                id = "doc-007",
                name = "drh. Raditya Pratama",
                specialization = "Gigi",
                practiceDays = "Kamis, Sabtu, Minggu",
                practiceHours = "11:00 - 17:00",
                phoneContact = "0819-2233-4455",
                clinicId = "clinic-003",
                clinicName = "Petcare Batu",
                isActive = false,
                experienceYears = 5,
                notes = "Sedang cuti seminar kedokteran hewan nasional."
            )
        )

        private val initialServices = mutableListOf(
            ClinicService(
                id = "srv-001",
                clinicId = "clinic-001",
                name = "Pemeriksaan Umum",
                description = "Pemeriksaan fisik menyeluruh meliputi suhu badan, berat, telinga, mata, dan detak jantung.",
                estimatedPrice = 75000,
                durationMinutes = 20,
                category = "Pemeriksaan",
                isActive = true
            ),
            ClinicService(
                id = "srv-002",
                clinicId = "clinic-001",
                name = "Vaksinasi Rabies",
                description = "Pemberian vaksin rabies tahunan untuk perlindungan kekebalan hewan peliharaan.",
                estimatedPrice = 150000,
                durationMinutes = 15,
                category = "Vaksinasi",
                isActive = true
            ),
            ClinicService(
                id = "srv-003",
                clinicId = "clinic-001",
                name = "Grooming Medis & Lengkap",
                description = "Mandi terapi anti-jamur/kutu, potong kuku, pembersihan telinga, dan pengeringan steril.",
                estimatedPrice = 120000,
                durationMinutes = 60,
                category = "Grooming",
                isActive = true
            ),
            ClinicService(
                id = "srv-004",
                clinicId = "clinic-001",
                name = "Rawat Inap Intensif",
                description = "Ruang isolasi ber-AC dilengkapi infus monitor dan pengawasan dokter 24 jam.",
                estimatedPrice = 150000,
                durationMinutes = 1440,
                category = "Rawat Inap",
                isActive = true
            ),
            ClinicService(
                id = "srv-005",
                clinicId = "clinic-001",
                name = "Operasi Minor & Sterilisasi",
                description = "Tindakan bedah minor dan kebiri steril aman dengan pembiusan inhalasi.",
                estimatedPrice = 450000,
                durationMinutes = 90,
                category = "Operasi",
                isActive = false
            ),
            ClinicService(
                id = "srv-006",
                clinicId = "clinic-002",
                name = "Pemeriksaan Umum & Kulit",
                description = "Pemeriksaan dermatologi, kerokan kulit mikroskopis, dan konsultasi alergi.",
                estimatedPrice = 85000,
                durationMinutes = 25,
                category = "Pemeriksaan",
                isActive = true
            ),
            ClinicService(
                id = "srv-007",
                clinicId = "clinic-002",
                name = "Vaksinasi Lengkap Tricat / Eurican",
                description = "Vaksinasi inti tahunan untuk kucing dan anjing disertai buku vaksin resmi.",
                estimatedPrice = 185000,
                durationMinutes = 20,
                category = "Vaksinasi",
                isActive = true
            ),
            ClinicService(
                id = "srv-008",
                clinicId = "clinic-003",
                name = "Pemeriksaan Umum",
                description = "Cek kesehatan rutin, timbang berat badan, dan pemberian vitamin.",
                estimatedPrice = 70000,
                durationMinutes = 20,
                category = "Pemeriksaan",
                isActive = true
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

    init {
        if (!hasFetchedInitialRemote && SupabaseConfig.isConfigured) {
            hasFetchedInitialRemote = true
            repoScope.launch {
                refreshAllFromSupabase()
            }
        }
    }

    suspend fun refreshAllFromSupabase() {
        if (!SupabaseConfig.isConfigured) return

        initialClinics.forEach { clinic ->
            SupabaseRestClient.insert(TABLE_CLINICS, clinicToJson(clinic))
            val encodedId = SupabaseRestClient.encodeParam(clinic.id)
            SupabaseRestClient.update(TABLE_CLINICS, "id=eq.$encodedId", clinicToJson(clinic))
        }
        initialDoctors.forEach { doc ->
            SupabaseRestClient.insert(TABLE_DOCTORS, doctorToJson(doc))
            val encodedId = SupabaseRestClient.encodeParam(doc.id)
            SupabaseRestClient.update(TABLE_DOCTORS, "id=eq.$encodedId", doctorToJson(doc))
        }
        initialServices.forEach { srv ->
            SupabaseRestClient.insert(TABLE_SERVICES, serviceToJson(srv))
        }

        SupabaseRestClient.getList(TABLE_CLINICS, "select=*").onSuccess { arr ->
            if (arr.length() > 0) {
                val loaded = mutableListOf<ClinicInfo>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", "")
                    if (id.isNotBlank() && loaded.none { it.id == id }) {
                        loaded.add(jsonToClinic(obj))
                    }
                }
                if (loaded.isNotEmpty()) {
                    initialClinics.clear()
                    initialClinics.addAll(loaded)
                    _clinicsFlow.value = initialClinics.toList()
                }
            }
        }

        SupabaseRestClient.getList(TABLE_DOCTORS, "select=*").onSuccess { arr ->
            if (arr.length() > 0) {
                val loaded = mutableListOf<Doctor>()
                for (i in 0 until arr.length()) {
                    loaded.add(jsonToDoctor(arr.getJSONObject(i)))
                }
                if (loaded.isNotEmpty()) {
                    initialDoctors.clear()
                    initialDoctors.addAll(loaded)
                    _doctorsFlow.value = initialDoctors.toList()
                }
            }
        }

        SupabaseRestClient.getList(TABLE_SERVICES, "select=*").onSuccess { arr ->
            if (arr.length() > 0) {
                val loaded = mutableListOf<ClinicService>()
                for (i in 0 until arr.length()) {
                    loaded.add(jsonToService(arr.getJSONObject(i), i))
                }
                if (loaded.isNotEmpty()) {
                    initialServices.clear()
                    initialServices.addAll(loaded)
                    _servicesFlow.value = initialServices.toList()
                }
            }
        }
    }

    // ==========================================
    // OPERASI DOKTER HEWAN (CRUD SUPABASE)
    // ==========================================

    suspend fun getDoctorById(id: String): Doctor? {
        return _doctorsFlow.value.find { it.id == id }
    }

    suspend fun addDoctor(doctor: Doctor): Result<Doctor> {
        return try {
            val newDoctor = if (doctor.id.isBlank()) doctor.copy(id = UUID.randomUUID().toString()) else doctor
            if (SupabaseConfig.isConfigured) {
                SupabaseRestClient.insert(TABLE_DOCTORS, doctorToJson(newDoctor))
            }
            initialDoctors.removeAll { it.id == newDoctor.id }
            initialDoctors.add(0, newDoctor)
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(newDoctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctor: Doctor): Result<Doctor> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(doctor.id)
                SupabaseRestClient.update(TABLE_DOCTORS, "id=eq.$encodedId", doctorToJson(doctor))
            }
            val index = initialDoctors.indexOfFirst { it.id == doctor.id }
            if (index != -1) {
                initialDoctors[index] = doctor
            } else {
                initialDoctors.add(0, doctor)
            }
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleDoctorStatus(id: String): Result<Doctor> {
        val current = initialDoctors.find { it.id == id }
            ?: return Result.failure(IllegalArgumentException("Data dokter tidak ditemukan."))
        val updated = current.copy(isActive = !current.isActive)
        return updateDoctor(updated)
    }

    suspend fun deleteDoctor(id: String): Result<Unit> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(id)
                SupabaseRestClient.delete(TABLE_DOCTORS, "id=eq.$encodedId")
            }
            initialDoctors.removeAll { it.id == id }
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // OPERASI LAYANAN KLINIK (CRUD SUPABASE)
    // ==========================================

    suspend fun addService(service: ClinicService): Result<ClinicService> {
        return try {
            val newService = if (service.id.isBlank()) service.copy(id = UUID.randomUUID().toString()) else service
            if (SupabaseConfig.isConfigured) {
                SupabaseRestClient.insert(TABLE_SERVICES, serviceToJson(newService))
            }
            initialServices.removeAll { it.id == newService.id }
            initialServices.add(0, newService)
            _servicesFlow.value = initialServices.toList()
            Result.success(newService)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateService(service: ClinicService): Result<ClinicService> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(service.id)
                SupabaseRestClient.update(TABLE_SERVICES, "id=eq.$encodedId", serviceToJson(service))
            }
            val index = initialServices.indexOfFirst { it.id == service.id }
            if (index != -1) {
                initialServices[index] = service
            } else {
                initialServices.add(0, service)
            }
            _servicesFlow.value = initialServices.toList()
            Result.success(service)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteService(id: String): Result<Unit> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(id)
                SupabaseRestClient.delete(TABLE_SERVICES, "id=eq.$encodedId")
            }
            initialServices.removeAll { it.id == id }
            _servicesFlow.value = initialServices.toList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // OPERASI MULTI-KLINIK (CRUD SUPABASE)
    // ==========================================

    fun selectClinic(clinicId: String) {
        _selectedClinicIdFlow.value = clinicId
    }

    suspend fun addClinic(clinic: ClinicInfo): Result<ClinicInfo> {
        return try {
            val newClinic = if (clinic.id.isBlank()) clinic.copy(id = UUID.randomUUID().toString()) else clinic
            if (SupabaseConfig.isConfigured) {
                SupabaseRestClient.insert(TABLE_CLINICS, clinicToJson(newClinic))
            }
            initialClinics.removeAll { it.id == newClinic.id }
            initialClinics.add(newClinic)
            _clinicsFlow.value = initialClinics.toList()
            _selectedClinicIdFlow.value = newClinic.id
            Result.success(newClinic)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClinicInfo(info: ClinicInfo): Result<ClinicInfo> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(info.id)
                SupabaseRestClient.update(TABLE_CLINICS, "id=eq.$encodedId", clinicToJson(info))
            }
            val index = initialClinics.indexOfFirst { it.id == info.id }
            if (index != -1) {
                initialClinics[index] = info
            } else {
                initialClinics.add(info)
            }
            _clinicsFlow.value = initialClinics.toList()
            // Update nama klinik pada dokter yang terdaftar di klinik ini
            val updatedDocs = initialDoctors.map { doc ->
                if (doc.clinicId == info.id) doc.copy(clinicName = info.name) else doc
            }
            initialDoctors.clear()
            initialDoctors.addAll(updatedDocs)
            _doctorsFlow.value = initialDoctors.toList()
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClinic(clinicId: String): Result<Unit> {
        if (initialClinics.size <= 1) {
            return Result.failure(IllegalStateException("Minimal harus tersisa 1 klinik aktif."))
        }
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(clinicId)
                SupabaseRestClient.delete(TABLE_CLINICS, "id=eq.$encodedId")
            }
            initialClinics.removeAll { it.id == clinicId }
            _clinicsFlow.value = initialClinics.toList()
            if (_selectedClinicIdFlow.value == clinicId) {
                _selectedClinicIdFlow.value = initialClinics.first().id
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun clinicToJson(c: ClinicInfo): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("name", c.name)
        put("address", c.address)
        put("phone_number", c.phoneNumber)
        put("operational_hours", c.operationalHours)
        put("emergency_available", c.emergencyAvailable)
        put("description", c.description)
    }

    private fun jsonToClinic(obj: JSONObject): ClinicInfo {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val emergency = obj.optBoolean("emergency_available", true)
        return ClinicInfo(
            id = id,
            name = obj.optString("name", ""),
            address = obj.optString("address", ""),
            phoneNumber = obj.optString("phone_number", ""),
            operationalHours = obj.optString("operational_hours", "08:00 - 20:00 WIB"),
            emergencyAvailable = emergency,
            isActive = id != "clinic-003" || emergency,
            description = obj.optString("description", "")
        )
    }

    private fun doctorToJson(d: Doctor): JSONObject = JSONObject().apply {
        put("id", d.id)
        put("name", d.name)
        put("specialization", d.specialization)
        put("practice_days", d.practiceDays)
        put("practice_hours", d.practiceHours)
        put("phone_contact", d.phoneContact)
        put("clinic_id", d.clinicId)
        put("clinic_name", d.clinicName)
        put("photo_uri", JSONObject.NULL)
        put("avatar_index", d.avatarIndex)
        put("is_active", d.isActive)
        put("experience_years", d.experienceYears)
        put("notes", d.notes)
    }

    private fun jsonToDoctor(obj: JSONObject): Doctor = Doctor(
        id = obj.optString("id", UUID.randomUUID().toString()),
        name = obj.optString("name", ""),
        specialization = obj.optString("specialization", ""),
        practiceDays = obj.optString("practice_days", "Senin, Rabu, Jumat"),
        practiceHours = obj.optString("practice_hours", "08:00 - 14:00"),
        phoneContact = obj.optString("phone_contact", ""),
        clinicId = obj.optString("clinic_id", "clinic-001"),
        clinicName = obj.optString("clinic_name", "Petcare Malang"),
        photoUri = null,
        avatarIndex = obj.optInt("avatar_index", 1),
        isActive = obj.optBoolean("is_active", true),
        experienceYears = obj.optInt("experience_years", 3),
        notes = obj.optString("notes", "")
    )

    private fun serviceToJson(s: ClinicService): JSONObject = JSONObject().apply {
        put("id", s.id)
        put("name", s.name)
        put("description", s.description)
        put("estimated_price", s.estimatedPrice)
        put("duration_minutes", s.durationMinutes)
        put("category", s.category)
    }

    private fun jsonToService(obj: JSONObject, index: Int): ClinicService {
        val id = obj.optString("id", UUID.randomUUID().toString())
        val existingLocal = initialServices.find { it.id == id }
        val defaultClinic = existingLocal?.clinicId ?: when (index % 3) {
            0 -> "clinic-001"
            1 -> "clinic-002"
            else -> "clinic-003"
        }
        return ClinicService(
            id = id,
            clinicId = defaultClinic,
            name = obj.optString("name", ""),
            description = obj.optString("description", ""),
            estimatedPrice = obj.optLong("estimated_price", 0L),
            durationMinutes = obj.optInt("duration_minutes", 20),
            category = obj.optString("category", "Pemeriksaan"),
            isActive = existingLocal?.isActive ?: true
        )
    }
}
