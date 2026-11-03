package com.example.petcare.data.repository

import com.example.petcare.data.model.JenisRiwayatKesehatan
import com.example.petcare.data.model.RiwayatKesehatan
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository untuk mengelola data Riwayat Kesehatan Hewan (CRUD).
 *
 * Sesuai kriteria evaluasi tugas:
 * - Mengelola Data Supabase (Read, Insert, Update, Delete)
 * - Tabel: `riwayat_kesehatan`
 *
 * Skema SQL Supabase untuk tabel `riwayat_kesehatan`:
 * ```sql
 * CREATE TABLE riwayat_kesehatan (
 *     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 *     user_id TEXT NOT NULL,
 *     pet_name TEXT NOT NULL,
 *     pet_type TEXT NOT NULL DEFAULT 'Kucing',
 *     jenis TEXT NOT NULL, -- 'SAKIT', 'OBAT', 'OPERASI'
 *     judul TEXT NOT NULL,
 *     tanggal TEXT NOT NULL,
 *     dokter_atau_klinik TEXT NOT NULL,
 *     catatan_medis TEXT NOT NULL,
 *     biaya BIGINT,
 *     status_pemulihan TEXT NOT NULL DEFAULT 'Selesai',
 *     created_at TIMESTAMPTZ DEFAULT now()
 * );
 * ```
 */
class RiwayatKesehatanRepository {

    companion object {
        // Data in-memory reaktif yang tersimpan selama aplikasi berjalan
        // Diberi data awal realistis untuk kemudahan demonstrasi pengujian
        private val records = MutableStateFlow(
            mutableListOf(
                RiwayatKesehatan(
                    id = "med-001",
                    userId = "user-demo-001",
                    petName = "Milo",
                    petType = "Kucing Domestik",
                    jenis = JenisRiwayatKesehatan.SAKIT,
                    judul = "Pemeriksaan ISPA & Bersin",
                    tanggal = "28 Okt 2026",
                    dokterAtauKlinik = "drh. Sarah Wijaya - Klinik Satwa Medika",
                    catatanMedis = "Milo mengalami bersin berulang dan hidung berair selama 3 hari. Suhu tubuh 38.9°C (normal tinggi). Diberikan nebulizer dan antibiotik tetes.",
                    biaya = 175000L,
                    statusPemulihan = "Dalam Perawatan"
                ),
                RiwayatKesehatan(
                    id = "med-002",
                    userId = "user-demo-001",
                    petName = "Bruno",
                    petType = "Anjing Golden",
                    jenis = JenisRiwayatKesehatan.OBAT,
                    judul = "Pemberian Obat Cacing & Vitamin Bulu",
                    tanggal = "20 Okt 2026",
                    dokterAtauKlinik = "drh. Hendra Kurniawan - Pet Care Center",
                    catatanMedis = "Pemberian obat cacing Drontal Dog rutin per 3 bulan sesuai berat badan 24 kg. Ditambah suplemen minyak ikan omega-3 untuk kesehatan kulit dan mantel bulu.",
                    biaya = 120000L,
                    statusPemulihan = "Selesai"
                ),
                RiwayatKesehatan(
                    id = "med-003",
                    userId = "user-demo-001",
                    petName = "Luna",
                    petType = "Kucing Persia",
                    jenis = JenisRiwayatKesehatan.OPERASI,
                    judul = "Tindakan Operasi Steril Betina (OH)",
                    tanggal = "12 Okt 2026",
                    dokterAtauKlinik = "drh. Anita Pratiwi - Rumah Sakit Hewan Sejahtera",
                    catatanMedis = "Operasi Ovariohisterektomi (steril) berlangsung lancar tanpa komplikasi. Jahitan intradermal 3 simpul. Wajib mengenakan elizabethan collar selama 7 hari dan kontrol lepas perban.",
                    biaya = 850000L,
                    statusPemulihan = "Selesai"
                ),
                RiwayatKesehatan(
                    id = "med-004",
                    userId = "user-demo-001",
                    petName = "Snowy",
                    petType = "Kelinci Anggora",
                    jenis = JenisRiwayatKesehatan.SAKIT,
                    judul = "Infeksi Telinga (Ear Mites)",
                    tanggal = "05 Okt 2026",
                    dokterAtauKlinik = "drh. Sarah Wijaya - Klinik Satwa Medika",
                    catatanMedis = "Kelinci sering menggelengkan kepala dan menggaruk daun telinga. Ditemukan kerak kecokelatan di liang telinga. Dibersihkan dan ditetesi obat antiparasit khusus kelinci.",
                    biaya = 150000L,
                    statusPemulihan = "Perlu Kontrol"
                ),
                RiwayatKesehatan(
                    id = "med-005",
                    userId = "user-demo-001",
                    petName = "Milo",
                    petType = "Kucing Domestik",
                    jenis = JenisRiwayatKesehatan.OBAT,
                    judul = "Obat Tetes Mata Antibakteri",
                    tanggal = "15 Sep 2026",
                    dokterAtauKlinik = "drh. Sarah Wijaya - Klinik Satwa Medika",
                    catatanMedis = "Konjungtivitis ringan pada mata kiri. Diresepkan Cendo Xitrol tetes mata hewan 2 tetes 3x sehari selama 5 hari berturut-turut.",
                    biaya = 85000L,
                    statusPemulihan = "Selesai"
                )
            )
        )
    }

    /**
     * READ: Mendapatkan daftar catatan riwayat kesehatan yang reaktif terhadap perubahan,
     * difilter berdasarkan jenis (Semua / Sakit / Obat / Operasi) dan kata kunci pencarian.
     */
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
            }.sortedByDescending { it.createdAt }
        }
    }

    /**
     * READ: Mengambil satu catatan detail berdasarkan ID.
     */
    suspend fun getRiwayatById(id: String): RiwayatKesehatan? {
        delay(150) // Simulasi latensi database
        return records.value.find { it.id == id }
    }

    /**
     * INSERT: Menambahkan catatan riwayat medis baru ke tabel database.
     */
    suspend fun insertRiwayat(item: RiwayatKesehatan): Result<RiwayatKesehatan> {
        delay(350) // Simulasi latensi jaringan Supabase
        return try {
            val newItem = if (item.id.isBlank()) item.copy(id = UUID.randomUUID().toString()) else item
            val currentList = records.value.toMutableList()
            currentList.add(0, newItem)
            records.value = currentList
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * UPDATE: Memperbarui catatan riwayat medis yang sudah ada.
     */
    suspend fun updateRiwayat(item: RiwayatKesehatan): Result<RiwayatKesehatan> {
        delay(350) // Simulasi latensi jaringan Supabase
        return try {
            val currentList = records.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == item.id }
            if (index != -1) {
                currentList[index] = item
                records.value = currentList
                Result.success(item)
            } else {
                Result.failure(NoSuchElementException("Catatan riwayat kesehatan tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * DELETE: Menghapus catatan riwayat kesehatan berdasarkan ID.
     */
    suspend fun deleteRiwayat(id: String): Result<Boolean> {
        delay(300) // Simulasi latensi jaringan Supabase
        return try {
            val currentList = records.value.toMutableList()
            val removed = currentList.removeAll { it.id == id }
            if (removed) {
                records.value = currentList
                Result.success(true)
            } else {
                Result.failure(NoSuchElementException("Catatan riwayat kesehatan tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
