package com.example.petcare.data.remote

import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stub SupabaseRestClient — Supabase belum dikonfigurasi untuk project ini.
 * File ini hanya untuk menghindari compile error saat build.
 * Semua method mengembalikan failure/null sehingga kode local fallback tetap berjalan.
 *
 * Ketika Supabase siap diintegrasikan, ganti file ini dengan implementasi asli.
 */
object SupabaseRestClient {

    /** Ambil list data dari tabel Supabase. Selalu gagal (Supabase tidak dikonfigurasi). */
    suspend fun getList(
        table: String,
        queryParams: String = ""
    ): Result<JSONArray> {
        return Result.failure(UnsupportedOperationException("Supabase tidak dikonfigurasi"))
    }

    /** Insert satu row ke tabel Supabase. Selalu gagal. */
    suspend fun insert(
        table: String,
        payload: JSONObject,
        upsert: Boolean = false
    ): Result<JSONObject> {
        return Result.failure(UnsupportedOperationException("Supabase tidak dikonfigurasi"))
    }

    /** Update row di tabel Supabase. Selalu gagal. */
    suspend fun update(
        table: String,
        matchQuery: String,
        payload: JSONObject
    ): Result<JSONObject> {
        return Result.failure(UnsupportedOperationException("Supabase tidak dikonfigurasi"))
    }

    /** Delete row dari tabel Supabase. Selalu gagal. */
    suspend fun delete(
        table: String,
        matchQuery: String
    ): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Supabase tidak dikonfigurasi"))
    }

    /** Encode parameter untuk query string Supabase. */
    fun encodeParam(value: String): String {
        return java.net.URLEncoder.encode(value, "UTF-8")
    }

    /** Download bitmap dari URL Supabase Storage. Selalu return null. */
    suspend fun downloadBitmap(url: String): Bitmap? {
        return null
    }

    /** Upload gambar ke Supabase Storage. Selalu gagal. */
    suspend fun uploadImage(
        objectPath: String,
        bytes: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> {
        return Result.failure(UnsupportedOperationException("Supabase tidak dikonfigurasi"))
    }
}
