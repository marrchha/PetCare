package com.example.petcare.data.repository

import com.example.petcare.data.model.JadwalVaksin
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class JadwalVaksinRepository(
    private val restClient: SupabaseRestClient = SupabaseRestClient()
) {
    companion object {
        private val localCache = mutableListOf(
            JadwalVaksin(
                id = "jv-001",
                userId = "user-demo-001",
                namaHewan = "Mimi (Kucing)",
                jenis = JadwalVaksin.JENIS_VAKSIN,
                keperluan = "Vaksin Tricat & Rabies",
                tanggal = "2026-11-12",
                jam = "09:30",
                klinik = "Klinik Satwa Sehat Husada",
                catatan = "Bawa buku vaksin sebelumnya. Hewan harus dalam keadaan sehat dan nafsu makan normal.",
                status = JadwalVaksin.STATUS_AKAN_DATANG
            ),
            JadwalVaksin(
                id = "jv-002",
                userId = "user-demo-001",
                namaHewan = "Rocky (Anjing)",
                jenis = JadwalVaksin.JENIS_KONTROL,
                keperluan = "Pemeriksaan Rutin & Cek Gigi",
                tanggal = "2026-11-18",
                jam = "14:00",
                klinik = "Pet Care Center & Hospital",
                catatan = "Pemeriksaan plak pada taring kanan atas dan konsultasi vitamin bulu.",
                status = JadwalVaksin.STATUS_AKAN_DATANG
            ),
            JadwalVaksin(
                id = "jv-003",
                userId = "user-demo-001",
                namaHewan = "Chiki (Kelinci)",
                jenis = JadwalVaksin.JENIS_KONTROL,
                keperluan = "Kontrol Pasca Pengobatan Telinga",
                tanggal = "2026-10-22",
                jam = "10:30",
                klinik = "Veterinary Holistic Care",
                catatan = "Telinga sudah bersih dari earmites, obat tetes dihentikan sesuai instruksi dokter.",
                status = JadwalVaksin.STATUS_SELESAI
            ),
            JadwalVaksin(
                id = "jv-004",
                userId = "user-demo-001",
                namaHewan = "Milo (Kucing)",
                jenis = JadwalVaksin.JENIS_VAKSIN,
                keperluan = "Vaksin FeLV Booster",
                tanggal = "2026-10-15",
                jam = "11:00",
                klinik = "Klinik Satwa Sehat Husada",
                catatan = "Sudah disuntik vaksin booster, tidak ada tanda-tanda alergi atau lemas.",
                status = JadwalVaksin.STATUS_SELESAI
            )
        )
    }

    suspend fun getJadwalList(userId: String? = null): Result<List<JadwalVaksin>> =
        withContext(Dispatchers.IO) {
            try {
                if (SupabaseConfig.isConfigured) {
                    val remoteList = restClient.fetchJadwalList(userId)
                    localCache.clear()
                    localCache.addAll(remoteList)
                    Result.success(remoteList)
                } else {
                    val filtered = if (!userId.isNullOrBlank()) {
                        localCache.filter { it.userId == userId || it.userId == "user-demo-001" }
                    } else {
                        localCache.toList()
                    }
                    Result.success(filtered)
                }
            } catch (e: Exception) {
                Result.success(localCache.toList())
            }
        }

    suspend fun getJadwalById(id: String): Result<JadwalVaksin?> = withContext(Dispatchers.IO) {
        try {
            val cached = localCache.find { it.id == id }
            Result.success(cached)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertJadwal(jadwal: JadwalVaksin): Result<JadwalVaksin> =
        withContext(Dispatchers.IO) {
            try {
                val prepared = if (jadwal.id.isBlank()) {
                    jadwal.copy(id = UUID.randomUUID().toString())
                } else {
                    jadwal
                }

                if (SupabaseConfig.isConfigured) {
                    try {
                        val inserted = restClient.insertJadwal(prepared)
                        localCache.removeAll { it.id == inserted.id }
                        localCache.add(0, inserted)
                        return@withContext Result.success(inserted)
                    } catch (e: Exception) {
                        localCache.add(0, prepared)
                        return@withContext Result.success(prepared)
                    }
                } else {
                    localCache.add(0, prepared)
                    Result.success(prepared)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun updateJadwal(jadwal: JadwalVaksin): Result<JadwalVaksin> =
        withContext(Dispatchers.IO) {
            try {
                val index = localCache.indexOfFirst { it.id == jadwal.id }
                if (index != -1) {
                    localCache[index] = jadwal
                } else {
                    localCache.add(jadwal)
                }

                if (SupabaseConfig.isConfigured) {
                    try {
                        val updated = restClient.updateJadwal(jadwal)
                        Result.success(updated)
                    } catch (e: Exception) {
                        Result.success(jadwal)
                    }
                } else {
                    Result.success(jadwal)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteJadwal(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localCache.removeAll { it.id == id }
            if (SupabaseConfig.isConfigured) {
                try {
                    restClient.deleteJadwal(id)
                } catch (_: Exception) {
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleStatus(id: String): Result<JadwalVaksin> = withContext(Dispatchers.IO) {
        val target = localCache.find { it.id == id }
            ?: return@withContext Result.failure(NoSuchElementException("Jadwal dengan ID $id tidak ditemukan"))

        val newStatus = if (target.isCompleted) {
            JadwalVaksin.STATUS_AKAN_DATANG
        } else {
            JadwalVaksin.STATUS_SELESAI
        }
        val updated = target.copy(status = newStatus)
        updateJadwal(updated)
    }
}
