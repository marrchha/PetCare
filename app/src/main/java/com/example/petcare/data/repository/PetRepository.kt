package com.example.petcare.data.repository

import android.content.Context
import android.net.Uri
import com.example.petcare.data.model.Pet
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class PetRepository {

    companion object {
        private const val TABLE_PETS = "pets"

        private val petStorage = mutableListOf(
            Pet(
                id = "pet-001",
                name = "Mochi",
                species = "Kucing",
                breed = "Scottish Fold",
                gender = "Betina",
                ageMonths = 14,
                weightKg = 3.8,
                color = "Putih Abu-abu",
                photoUri = null,
                notes = "Sangat manja, alergi makanan laut tertentu, suka tidur di dekat jendela.",
                accentIndex = 0
            ),
            Pet(
                id = "pet-002",
                name = "Buddy",
                species = "Anjing",
                breed = "Golden Retriever",
                gender = "Jantan",
                ageMonths = 24,
                weightKg = 26.5,
                color = "Kemasannya Keemasan (Golden)",
                photoUri = null,
                notes = "Aktif bermain lempar bola setiap sore. Sudah vaksin rabies lengkap.",
                accentIndex = 1
            ),
            Pet(
                id = "pet-003",
                name = "Luna",
                species = "Kucing",
                breed = "British Shorthair",
                gender = "Betina",
                ageMonths = 10,
                weightKg = 3.2,
                color = "Biru Keabu-abuan (Blue Grey)",
                photoUri = null,
                notes = "Tenang dan bersih. Jadwal grooming rutin setiap 2 minggu sekali.",
                accentIndex = 2
            ),
            Pet(
                id = "pet-004",
                name = "Coco",
                species = "Kelinci",
                breed = "Holland Lop",
                gender = "Jantan",
                ageMonths = 8,
                weightKg = 1.6,
                color = "Cokelat Karamel & Putih",
                photoUri = null,
                notes = "Suka rumput Timothy Hay segar dan potongan wortel kecil.",
                accentIndex = 0
            )
        )
    }

    fun getAllPets(): List<Pet> {
        return petStorage.toList()
    }

    fun getPetById(petId: String): Pet? {
        return petStorage.find { it.id == petId }
    }

    /**
     * READ dari Supabase: Mengambil semua data hewan dari tabel `pets`.
     * Jika tabel di Supabase masih kosong, otomatis melakukan seed data awal.
     */
    suspend fun fetchAllPets(): Result<List<Pet>> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.isConfigured) {
            return@withContext Result.success(petStorage.toList())
        }

        val response = SupabaseRestClient.getList(
            table = TABLE_PETS,
            queryParams = "select=*&order=created_at.desc"
        )

        response.fold(
            onSuccess = { jsonArray ->
                if (jsonArray.length() == 0 && petStorage.isNotEmpty()) {
                    petStorage.forEach { seedPet ->
                        SupabaseRestClient.insert(TABLE_PETS, petToJson(seedPet))
                    }
                    Result.success(petStorage.toList())
                } else {
                    val loaded = mutableListOf<Pet>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        loaded.add(jsonToPet(obj, i))
                    }
                    petStorage.clear()
                    petStorage.addAll(loaded)
                    Result.success(petStorage.toList())
                }
            },
            onFailure = {
                Result.success(petStorage.toList())
            }
        )
    }

    /**
     * UPLOAD GAMBAR ke Supabase Storage (`petcare-media/pets/pet_{id}.jpg`).
     * Mengembalikan URL publik gambar dari Supabase Storage.
     */
    suspend fun uploadPetPhotoIfNeeded(
        context: Context,
        petId: String,
        photoUri: String?
    ): String? = withContext(Dispatchers.IO) {
        if (photoUri.isNullOrBlank()) return@withContext null
        if (photoUri.startsWith("http://") || photoUri.startsWith("https://")) {
            return@withContext photoUri
        }
        if (!SupabaseConfig.isConfigured) {
            return@withContext photoUri
        }

        runCatching {
            val parsedUri = Uri.parse(photoUri)
            val bytes = context.contentResolver.openInputStream(parsedUri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) {
                val uploadedUrl = SupabaseRestClient.uploadImage(
                    objectPath = "pets/pet_$petId.jpg",
                    bytes = bytes,
                    contentType = "image/jpeg"
                ).getOrNull()
                uploadedUrl ?: photoUri
            } else {
                photoUri
            }
        }.getOrDefault(photoUri)
    }

    /**
     * INSERT ke tabel `pets` di Supabase.
     */
    suspend fun insertPetRemote(
        context: Context,
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageMonths: Int,
        weightKg: Double,
        color: String,
        photoUri: String?,
        notes: String
    ): Result<Pet> = withContext(Dispatchers.IO) {
        val newId = "pet-${UUID.randomUUID().toString().take(8)}"
        val finalPhotoUrl = uploadPetPhotoIfNeeded(context, newId, photoUri)
        val newPet = Pet(
            id = newId,
            name = name.trim(),
            species = species.trim(),
            breed = breed.trim(),
            gender = gender,
            ageMonths = ageMonths,
            weightKg = weightKg,
            color = color.trim(),
            photoUri = finalPhotoUrl,
            notes = notes.trim(),
            accentIndex = petStorage.size % 3
        )

        if (SupabaseConfig.isConfigured) {
            SupabaseRestClient.insert(
                table = TABLE_PETS,
                payload = petToJson(newPet)
            ).onFailure { err ->
                return@withContext Result.failure(err)
            }
        }

        petStorage.removeAll { it.id == newPet.id }
        petStorage.add(0, newPet)
        Result.success(newPet)
    }

    /**
     * UPDATE ke tabel `pets` di Supabase.
     */
    suspend fun updatePetRemote(
        context: Context,
        updatedPet: Pet
    ): Result<Pet> = withContext(Dispatchers.IO) {
        val finalPhotoUrl = uploadPetPhotoIfNeeded(context, updatedPet.id, updatedPet.photoUri)
        val finalPet = updatedPet.copy(photoUri = finalPhotoUrl)

        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(finalPet.id)
            SupabaseRestClient.update(
                table = TABLE_PETS,
                matchQuery = "id=eq.$encodedId",
                payload = petToJson(finalPet)
            ).onFailure { err ->
                return@withContext Result.failure(err)
            }
        }

        val index = petStorage.indexOfFirst { it.id == finalPet.id }
        if (index != -1) {
            petStorage[index] = finalPet
        } else {
            petStorage.add(0, finalPet)
        }
        Result.success(finalPet)
    }

    /**
     * DELETE dari tabel `pets` di Supabase.
     */
    suspend fun deletePetRemote(petId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(petId)
            SupabaseRestClient.delete(
                table = TABLE_PETS,
                matchQuery = "id=eq.$encodedId"
            ).onFailure { err ->
                return@withContext Result.failure(err)
            }
        }
        val removed = petStorage.removeAll { it.id == petId }
        Result.success(removed)
    }

    private fun petToJson(pet: Pet): JSONObject {
        return JSONObject().apply {
            put("id", pet.id)
            put("name", pet.name)
            put("species", pet.species)
            put("breed", pet.breed)
            put("gender", pet.gender)
            put("age_months", pet.ageMonths)
            put("weight_kg", pet.weightKg)
            put("color", pet.color)
            put("photo_uri", pet.photoUri ?: JSONObject.NULL)
            put("notes", pet.notes)
            put("accent_index", pet.accentIndex)
        }
    }

    private fun jsonToPet(obj: JSONObject, indexFallback: Int): Pet {
        val rawPhoto = obj.optString("photo_uri", "")
        return Pet(
            id = obj.optString("id", "pet-${UUID.randomUUID().toString().take(8)}"),
            name = obj.optString("name", ""),
            species = obj.optString("species", "Kucing"),
            breed = obj.optString("breed", ""),
            gender = obj.optString("gender", "Betina"),
            ageMonths = obj.optInt("age_months", 0),
            weightKg = obj.optDouble("weight_kg", 0.0),
            color = obj.optString("color", ""),
            photoUri = if (rawPhoto.isBlank() || rawPhoto == "null") null else rawPhoto,
            notes = obj.optString("notes", ""),
            accentIndex = obj.optInt("accent_index", indexFallback % 3)
        )
    }
}
