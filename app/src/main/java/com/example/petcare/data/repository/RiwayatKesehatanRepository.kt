package com.example.petcare.data.repository

import com.example.petcare.data.model.JenisRiwayatKesehatan
import com.example.petcare.data.model.RiwayatKesehatan
import com.example.petcare.data.remote.SequentialIdGenerator
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Repository untuk mengelola data Riwayat Kesehatan Hewan (Fitur 4 - Givenia Marudur Hasiana).
 * Terintegrasi penuh dengan tabel Supabase `riwayat_kesehatan` (Read, Insert, Update, Delete),
 * menggunakan ID berurutan (med-001, med-002, dst.), dan berelasi dengan Daftar Dokter (Fitur 2)
 * serta Daftar Hewan (Fitur 5).
 */
class RiwayatKesehatanRepository {

    companion object {
        private const val TABLE_HEALTH = "riwayat_kesehatan"
        private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var hasFetchedRemote = false

        private val records = MutableStateFlow(
            mutableListOf(
                RiwayatKesehatan(
                    id = "med-001",
                    userId = "user-001",
                    petName = "Mochi",
                    petType = "Kucing",
                    jenis = JenisRiwayatKesehatan.SAKIT,
                    judul = "Pemeriksaan ISPA & Bersin",
                    tanggal = "2026-10-28",
                    dokterAtauKlinik = "drh. Andi Prasetya - Petcare Malang",
                    catatanMedis = "Mochi mengalami bersin berulang dan hidung berair selama 3 hari. Suhu tubuh 38.9°C (normal tinggi). Diberikan nebulizer dan antibiotik tetes.",
                    biaya = 175000L,
                    statusPemulihan = "Dalam Perawatan",
                    createdAt = 1730000005000L
                ),
                RiwayatKesehatan(
                    id = "med-002",
                    userId = "user-001",
                    petName = "Buddy",
                    petType = "Anjing",
                    jenis = JenisRiwayatKesehatan.OBAT,
                    judul = "Pemberian Obat Cacing & Vitamin Bulu",
                    tanggal = "2026-10-20",
                    dokterAtauKlinik = "drh. Budi Santoso - Petcare Surabaya",
                    catatanMedis = "Pemberian obat cacing Drontal Dog rutin per 3 bulan sesuai berat badan 26.5 kg. Ditambah suplemen minyak ikan omega-3 untuk kesehatan kulit dan mantel bulu.",
                    biaya = 120000L,
                    statusPemulihan = "Selesai",
                    createdAt = 1730000004000L
                ),
                RiwayatKesehatan(
                    id = "med-003",
                    userId = "user-001",
                    petName = "Luna",
                    petType = "Kucing",
                    jenis = JenisRiwayatKesehatan.OPERASI,
                    judul = "Tindakan Operasi Steril Betina (OH)",
                    tanggal = "2026-10-12",
                    dokterAtauKlinik = "drh. Sarah Wijayanti - Petcare Malang",
                    catatanMedis = "Operasi Ovariohisterektomi (steril) berlangsung lancar tanpa komplikasi. Jahitan intradermal 3 simpul. Wajib mengenakan elizabethan collar selama 7 hari dan kontrol lepas perban.",
                    biaya = 850000L,
                    statusPemulihan = "Selesai",
                    createdAt = 1730000003000L
                ),
                RiwayatKesehatan(
                    id = "med-004",
                    userId = "user-001",
                    petName = "Coco",
                    petType = "Kelinci",
                    jenis = JenisRiwayatKesehatan.SAKIT,
                    judul = "Infeksi Telinga (Ear Mites)",
                    tanggal = "2026-10-05",
                    dokterAtauKlinik = "drh. Citra Maharani - Petcare Batu",
                    catatanMedis = "Kelinci sering menggelengkan kepala dan menggaruk daun telinga. Ditemukan kerak kecokelatan di liang telinga. Dibersihkan dan ditetesi obat antiparasit khusus kelinci.",
                    biaya = 150000L,
                    statusPemulihan = "Perlu Kontrol",
                    createdAt = 1730000002000L
                ),
                RiwayatKesehatan(
                    id = "med-005",
                    userId = "user-001",
                    petName = "Mochi",
                    petType = "Kucing",
                    jenis = JenisRiwayatKesehatan.OBAT,
                    judul = "Obat Tetes Mata Antibakteri",
                    tanggal = "2026-09-15",
                    dokterAtauKlinik = "drh. Andi Prasetya - Petcare Malang",
                    catatanMedis = "Konjungtivitis ringan pada mata kiri. Diresepkan Cendo Xitrol tetes mata hewan 2 tetes 3x sehari selama 5 hari berturut-turut.",
                    biaya = 85000L,
                    statusPemulihan = "Selesai",
                    createdAt = 1730000001000L
                )
            )
        )
    }

    init {
        if (!hasFetchedRemote && SupabaseConfig.isConfigured) {
            hasFetchedRemote = true
            repoScope.launch {
                refreshFromSupabase()
            }
        }
    }

    suspend fun refreshFromSupabase() {
        if (!SupabaseConfig.isConfigured) return
        val remoteRes = SupabaseRestClient.getList(
            table = TABLE_HEALTH,
            queryParams = "select=*&order=id.asc"
        )
        remoteRes.onSuccess { arr ->
            if (arr.length() == 0 && records.value.isNotEmpty()) {
                records.value.forEach { seed ->
                    SupabaseRestClient.insert(TABLE_HEALTH, riwayatToJson(seed))
                }
            } else if (arr.length() > 0) {
                val loaded = mutableListOf<RiwayatKesehatan>()
                for (i in 0 until arr.length()) {
                    loaded.add(jsonToRiwayat(arr.getJSONObject(i), i))
                }
                loaded.sortBy { it.id }
                records.value = loaded
            }
        }
    }

    fun getRiwayatList(
        filterJenis: JenisRiwayatKesehatan = JenisRiwayatKesehatan.SEMUA,
        searchQuery: String = ""
    ): Flow<List<RiwayatKesehatan>> {
        return records.map { list ->
            list.filter { item ->
                val matchesFilter = if (filterJenis == JenisRiwayatKesehatan.SEMUA) {
                    true
                } else {
                    item.jenis == filterJenis
                }

                val query = searchQuery.trim().lowercase()
                val matchesQuery = if (query.isEmpty()) {
                    true
                } else {
                    item.petName.lowercase().contains(query) ||
                        item.judul.lowercase().contains(query) ||
                        item.dokterAtauKlinik.lowercase().contains(query) ||
                        item.catatanMedis.lowercase().contains(query)
                }

                matchesFilter && matchesQuery
            }
        }
    }

    suspend fun getRiwayatById(id: String): RiwayatKesehatan? {
        return records.value.find { it.id == id }
    }

    suspend fun insertRiwayat(item: RiwayatKesehatan): Result<RiwayatKesehatan> {
        return try {
            val sequentialId = if (item.id.isBlank() || !Regex("^med-\\d+$").matches(item.id)) {
                SequentialIdGenerator.nextId(
                    table = TABLE_HEALTH,
                    prefix = "med",
                    localIds = records.value.map { it.id }
                )
            } else {
                item.id
            }
            val newItem = item.copy(id = sequentialId)
            if (SupabaseConfig.isConfigured) {
                SupabaseRestClient.insert(
                    table = TABLE_HEALTH,
                    payload = riwayatToJson(newItem)
                ).getOrThrow()
            }
            val currentList = records.value.toMutableList()
            currentList.removeAll { it.id == newItem.id }
            currentList.add(newItem)
            currentList.sortBy { it.id }
            records.value = currentList
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRiwayat(item: RiwayatKesehatan): Result<RiwayatKesehatan> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(item.id)
                SupabaseRestClient.update(
                    table = TABLE_HEALTH,
                    matchQuery = "id=eq.$encodedId",
                    payload = riwayatToJson(item)
                ).getOrThrow()
            }
            val currentList = records.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == item.id }
            if (index != -1) {
                currentList[index] = item
            } else {
                currentList.add(item)
            }
            currentList.sortBy { it.id }
            records.value = currentList
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRiwayat(id: String): Result<Boolean> {
        return try {
            if (SupabaseConfig.isConfigured) {
                val encodedId = SupabaseRestClient.encodeParam(id)
                SupabaseRestClient.delete(
                    table = TABLE_HEALTH,
                    matchQuery = "id=eq.$encodedId"
                ).getOrThrow()
            }
            val currentList = records.value.toMutableList()
            val removed = currentList.removeAll { it.id == id }
            records.value = currentList
            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun riwayatToJson(item: RiwayatKesehatan): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("user_id", item.userId)
        put("pet_name", item.petName)
        put("pet_type", item.petType)
        put("jenis", item.jenis.name)
        put("judul", item.judul)
        put("tanggal", item.tanggal)
        put("dokter_atau_klinik", item.dokterAtauKlinik)
        put("catatan_medis", item.catatanMedis)
        put("biaya", item.biaya ?: JSONObject.NULL)
        put("status_pemulihan", item.statusPemulihan)
        put("created_at", item.createdAt)
    }

    private fun jsonToRiwayat(obj: JSONObject, indexFallback: Int): RiwayatKesehatan {
        val biayaVal = if (obj.isNull("biaya")) null else obj.optLong("biaya", 0L)
        return RiwayatKesehatan(
            id = obj.optString("id", String.format("med-%03d", indexFallback + 1)),
            userId = obj.optString("user_id", "user-001"),
            petName = obj.optString("pet_name", ""),
            petType = obj.optString("pet_type", "Kucing"),
            jenis = JenisRiwayatKesehatan.fromString(obj.optString("jenis", "SAKIT")),
            judul = obj.optString("judul", ""),
            tanggal = obj.optString("tanggal", ""),
            dokterAtauKlinik = obj.optString("dokter_atau_klinik", ""),
            catatanMedis = obj.optString("catatan_medis", ""),
            biaya = biayaVal,
            statusPemulihan = obj.optString("status_pemulihan", "Selesai"),
            createdAt = obj.optLong("created_at", System.currentTimeMillis())
        )
    }
}
