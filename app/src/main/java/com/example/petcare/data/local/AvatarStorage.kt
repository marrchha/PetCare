package com.example.petcare.data.local

import android.content.Context
import android.net.Uri
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

    fun delete(userId: String): Boolean {
        val file = find(userId)
        return file?.delete() ?: false
    }

    fun find(userId: String): File? {
        val file = File(avatarDir, "avatar_$userId.jpg")
        return if (file.exists()) file else null
    }
}
