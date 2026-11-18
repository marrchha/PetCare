package com.example.petcare.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SupabaseRestClient {

    private val bitmapCache = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun encodeParam(value: String): String = URLEncoder.encode(value, "UTF-8")

    suspend fun getList(
        table: String,
        queryParams: String = "select=*"
    ): Result<JSONArray> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$table?$queryParams"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Accept", "application/json")
            }

            try {
                val code = connection.responseCode
                val body = readStream(connection, code)
                if (code in 200..299) {
                    if (body.isBlank()) JSONArray() else JSONArray(body)
                } else {
                    throw IllegalStateException("Supabase GET $table gagal ($code): $body")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun insert(
        table: String,
        payload: JSONObject,
        upsert: Boolean = true
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$table"
            val preferHeader = if (upsert) {
                "return=representation,resolution=merge-duplicates"
            } else {
                "return=representation"
            }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Prefer", preferHeader)
            }

            try {
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                val code = connection.responseCode
                val body = readStream(connection, code)
                if (code in 200..299) {
                    val arr = if (body.trim().startsWith("[")) JSONArray(body) else null
                    arr?.optJSONObject(0) ?: payload
                } else {
                    throw IllegalStateException("Supabase POST $table gagal ($code): $body")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun update(
        table: String,
        matchQuery: String,
        payload: JSONObject
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$table?$matchQuery"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                doOutput = true
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Prefer", "return=representation")
            }

            try {
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray(Charsets.UTF_8))
                }
                val code = connection.responseCode
                val body = readStream(connection, code)
                if (code in 200..299) {
                    val arr = if (body.trim().startsWith("[")) JSONArray(body) else null
                    arr?.optJSONObject(0) ?: payload
                } else {
                    throw IllegalStateException("Supabase PATCH $table gagal ($code): $body")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun delete(
        table: String,
        matchQuery: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${SupabaseConfig.SUPABASE_URL}/rest/v1/$table?$matchQuery"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
            }

            try {
                val code = connection.responseCode
                if (code in 200..299) {
                    true
                } else {
                    val body = readStream(connection, code)
                    throw IllegalStateException("Supabase DELETE $table gagal ($code): $body")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun uploadImage(
        objectPath: String,
        bytes: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanPath = objectPath.trimStart('/')
            val endpoint = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/${SupabaseConfig.STORAGE_BUCKET}/$cleanPath"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Content-Type", contentType)
                setRequestProperty("x-upsert", "true")
            }

            try {
                connection.outputStream.use { os ->
                    os.write(bytes)
                }
                val code = connection.responseCode
                if (code in 200..299) {
                    val publicUrl = "${SupabaseConfig.SUPABASE_URL}/storage/v1/object/public/${SupabaseConfig.STORAGE_BUCKET}/$cleanPath?t=${System.currentTimeMillis()}"
                    publicUrl
                } else {
                    val err = readStream(connection, code)
                    throw IllegalStateException("Upload gambar ke Supabase Storage gagal ($code): $err")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun downloadBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        bitmapCache.get(imageUrl)?.let { return@withContext it }
        runCatching {
            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            try {
                if (connection.responseCode in 200..299) {
                    connection.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)?.also { bmp ->
                            bitmapCache.put(imageUrl, bmp)
                        }
                    }
                } else {
                    null
                }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun readStream(connection: HttpURLConnection, responseCode: Int): String {
        val stream = if (responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: return ""
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
    }
}
