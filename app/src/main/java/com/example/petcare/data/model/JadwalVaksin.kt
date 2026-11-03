package com.example.petcare.data.model

import org.json.JSONObject
import java.util.UUID

/**
 * Model data untuk jadwal vaksin dan kontrol hewan peliharaan.
 * Mewakili tabel `jadwal_vaksin` pada Supabase.
 */
data class JadwalVaksin(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val namaHewan: String = "",
    val jenis: String = "Vaksin", // "Vaksin" atau "Kontrol"
    val keperluan: String = "", // nama vaksin atau alasan kontrol
    val tanggal: String = "", // contoh: "2026-11-15"
    val jam: String = "", // contoh: "10:00"
    val klinik: String = "",
    val catatan: String = "",
    val status: String = STATUS_AKAN_DATANG, // "Akan Datang" atau "Selesai"
    val createdAt: String = ""
) {
    val isCompleted: Boolean
        get() = status.equals(STATUS_SELESAI, ignoreCase = true)

    companion object {
        const val STATUS_AKAN_DATANG = "Akan Datang"
        const val STATUS_SELESAI = "Selesai"

        const val JENIS_VAKSIN = "Vaksin"
        const val JENIS_KONTROL = "Kontrol"

        fun fromJson(json: JSONObject): JadwalVaksin {
            return JadwalVaksin(
                id = json.optString("id", UUID.randomUUID().toString()),
                userId = json.optString("user_id", ""),
                namaHewan = json.optString("nama_hewan", ""),
                jenis = json.optString("jenis", JENIS_VAKSIN),
                keperluan = json.optString("keperluan", ""),
                tanggal = json.optString("tanggal", ""),
                jam = json.optString("jam", ""),
                klinik = json.optString("klinik", ""),
                catatan = json.optString("catatan", ""),
                status = json.optString("status", STATUS_AKAN_DATANG),
                createdAt = json.optString("created_at", "")
            )
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("user_id", userId)
            put("nama_hewan", namaHewan)
            put("jenis", jenis)
            put("keperluan", keperluan)
            put("tanggal", tanggal)
            put("jam", jam)
            put("klinik", klinik)
            put("catatan", catatan)
            put("status", status)
        }
    }
}
