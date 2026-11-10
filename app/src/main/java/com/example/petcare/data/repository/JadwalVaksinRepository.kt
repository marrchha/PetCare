package com.example.petcare.data.repository

import com.example.petcare.data.model.JadwalVaksin
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository untuk mengelola data jadwal vaksin dan kontrol hewan peliharaan (Fitur 1 - Sabitha Marsyarira Nayla).
 * Terintegrasi dengan tabel Supabase `jadwal_vaksin` (Read, Insert, Update, Delete).
 */
class JadwalVaksinRepository {

    companion object {
        private const val TABLE_JADWAL = "jadwal_vaksin"

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
            if (SupabaseConfig.isConfigured) {
                val remoteRes = SupabaseRestClient.getList(
                    table = TABLE_JADWAL,
                    queryParams = "select=*&order=created_at.desc"
                )
                remoteRes.onSuccess { arr ->
                    if (arr.length() == 0 && localCache.isNotEmpty()) {
                        localCache.forEach { seed ->
                            SupabaseRestClient.insert(TABLE_JADWAL, seed.toJson())
                        }
                    } else if (arr.length() > 0) {
                        val list = mutableListOf<JadwalVaksin>()
                        for (i in 0 until arr.length()) {
                            list.add(JadwalVaksin.fromJson(arr.getJSONObject(i)))
                        }
                        localCache.clear()
                        localCache.addAll(list)
                    }
                }
            }
            Result.success(localCache.toList())
        }

    suspend fun getJadwalById(id: String): Result<JadwalVaksin?> = withContext(Dispatchers.IO) {
        val cached = localCache.find { it.id == id }
        if (cached != null) return@withContext Result.success(cached)

        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(id)
            val arr = SupabaseRestClient.getList(
                table = TABLE_JADWAL,
                queryParams = "select=*&id=eq.$encodedId"
            ).getOrNull()
            if (arr != null && arr.length() > 0) {
                return@withContext Result.success(JadwalVaksin.fromJson(arr.getJSONObject(0)))
            }
        }
        Result.success(null)
    }

    suspend fun insertJadwal(jadwal: JadwalVaksin): Result<JadwalVaksin> =
        withContext(Dispatchers.IO) {
            val prepared = if (jadwal.id.isBlank()) {
                jadwal.copy(id = UUID.randomUUID().toString())
            } else {
                jadwal
            }

            if (SupabaseConfig.isConfigured) {
                SupabaseRestClient.insert(
                    table = TABLE_JADWAL,
                    payload = prepared.toJson()
                ).onFailure { err ->
                    return@withContext Result.failure(err)
                }
            }

            localCache.removeAll { it.id == prepared.id }
            localCache.add(0, prepared)
            Result.success(prepared)
        }

    suspend fun updateJadwal(jadwal: JadwalVaksin): Result<JadwalVaksin> =
        withContext(Dispatchers.IO) {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(jadwal.id)
                SupabaseRestClient.update(
                    table = TABLE_JADWAL,
                    matchQuery = "id=eq.$encodedId",
                    payload = jadwal.toJson()
                ).onFailure { err ->
                    return@withContext Result.failure(err)
                }
            }

            val index = localCache.indexOfFirst { it.id == jadwal.id }
            if (index != -1) {
                localCache[index] = jadwal
            } else {
                localCache.add(0, jadwal)
            }
            Result.success(jadwal)
        }

    suspend fun deleteJadwal(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(id)
            SupabaseRestClient.delete(
                table = TABLE_JADWAL,
                matchQuery = "id=eq.$encodedId"
            ).onFailure { err ->
                return@withContext Result.failure(err)
            }
        }
        localCache.removeAll { it.id == id }
        Result.success(Unit)
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
