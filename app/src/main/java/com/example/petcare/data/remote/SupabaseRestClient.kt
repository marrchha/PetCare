package com.example.petcare.data.remote

import com.example.petcare.data.model.JadwalVaksin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Klien HTTP PostgREST untuk berinteraksi langsung dengan REST API Supabase.
 * Menggunakan standard library Android tanpa membutuhkan dependensi eksternal tambahan.
 */
class SupabaseRestClient {

    private fun getBaseRestUrl(): String {
        val trimmed = SupabaseConfig.SUPABASE_URL.trimEnd('/')
        return "$trimmed/rest/v1/${SupabaseConfig.TABLE_JADWAL_VAKSIN}"
    }

    private fun openConnection(urlString: String, method: String): HttpURLConnection {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
        conn.setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("Prefer", "return=representation")
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        return conn
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val reader = BufferedReader(InputStreamReader(stream ?: conn.inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line)
        }
        reader.close()
        if (code !in 200..299) {
            throw IllegalStateException("Supabase error (HTTP $code): $sb")
        }
        return sb.toString()
    }

    suspend fun fetchJadwalList(userId: String?): List<JadwalVaksin> = withContext(Dispatchers.IO) {
        val queryBuilder = StringBuilder(getBaseRestUrl())
        val params = mutableListOf<String>()
        if (!userId.isNullOrBlank()) {
            params.add("user_id=eq.${URLEncoder.encode(userId, "UTF-8")}")
        }
        params.add("order=tanggal.asc,jam.asc")
        queryBuilder.append("?").append(params.joinToString("&"))

        val conn = openConnection(queryBuilder.toString(), "GET")
        try {
            val response = readResponse(conn)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<JadwalVaksin>()
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                list.add(JadwalVaksin.fromJson(itemObj))
            }
            list
        } finally {
            conn.disconnect()
        }
    }

    suspend fun insertJadwal(jadwal: JadwalVaksin): JadwalVaksin = withContext(Dispatchers.IO) {
        val conn = openConnection(getBaseRestUrl(), "POST")
        conn.doOutput = true
        try {
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jadwal.toJson().toString())
                writer.flush()
            }
            val response = readResponse(conn)
            val array = JSONArray(response)
            if (array.length() > 0) {
                JadwalVaksin.fromJson(array.getJSONObject(0))
            } else {
                jadwal
            }
        } finally {
            conn.disconnect()
        }
    }

    suspend fun updateJadwal(jadwal: JadwalVaksin): JadwalVaksin = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(jadwal.id, "UTF-8")
        val endpoint = "${getBaseRestUrl()}?id=eq.$encodedId"
        val conn = openConnection(endpoint, "PATCH")
        conn.doOutput = true
        try {
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jadwal.toJson().toString())
                writer.flush()
            }
            val response = readResponse(conn)
            val array = JSONArray(response)
            if (array.length() > 0) {
                JadwalVaksin.fromJson(array.getJSONObject(0))
            } else {
                jadwal
            }
        } finally {
            conn.disconnect()
        }
    }

    suspend fun deleteJadwal(id: String): Unit = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(id, "UTF-8")
        val endpoint = "${getBaseRestUrl()}?id=eq.$encodedId"
        val conn = openConnection(endpoint, "DELETE")
        try {
            readResponse(conn)
        } finally {
            conn.disconnect()
        }
    }
}
