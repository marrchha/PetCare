package com.example.petcare.data.local

import android.content.Context
import android.net.Uri
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import java.io.File
import java.io.FileOutputStream

class AvatarStorage(private val context: Context) {
    private val avatarDir: File
        get() = File(context.filesDir, "avatars").also { if (!it.exists()) it.mkdirs() }

    fun save(userId: String, uri: Uri): File {
        val file = File(avatarDir, "avatar_$userId.jpg")
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    /**
     * Mengunggah foto profil ke Supabase Storage (`petcare-media/avatars/avatar_{userId}.jpg`)
     * dan menyimpan salinan lokalnya sebagai fallback.
     */
    suspend fun saveAndUpload(userId: String, uri: Uri): String {
        val localFile = save(userId, uri)
        if (SupabaseConfig.isConfigured && localFile.exists()) {
            val bytes = localFile.readBytes()
            val uploadedUrl = SupabaseRestClient.uploadImage(
                objectPath = "avatars/avatar_$userId.jpg",
                bytes = bytes,
                contentType = "image/jpeg"
            ).getOrNull()
            if (!uploadedUrl.isNullOrBlank()) {
                return uploadedUrl
            }
        }
        return localFile.absolutePath
    }

    fun delete(userId: String): Boolean {
        val file = find(userId)
        return file?.delete() ?: false
    }

    fun find(userId: String): File? {
        val file = File(avatarDir, "avatar_$userId.jpg")
        return if (file.exists()) file else null
    }
}
