package com.example.petcare.data.repository

import com.example.petcare.data.model.Pet
import java.util.UUID
class PetRepository {

    companion object {
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

    fun insertPet(
        name: String,
        species: String,
        breed: String,
        gender: String,
        ageMonths: Int,
        weightKg: Double,
        color: String,
        photoUri: String?,
        notes: String
    ): Pet {
        val newPet = Pet(
            id = "pet-${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            species = species.trim(),
            breed = breed.trim(),
            gender = gender,
            ageMonths = ageMonths,
            weightKg = weightKg,
            color = color.trim(),
            photoUri = photoUri,
            notes = notes.trim(),
            accentIndex = petStorage.size % 3
        )
        petStorage.add(0, newPet)
        return newPet
    }

    fun updatePet(updatedPet: Pet): Boolean {
        val index = petStorage.indexOfFirst { it.id == updatedPet.id }
        return if (index != -1) {
            petStorage[index] = updatedPet
            true
        } else {
            false
        }
    }

    fun deletePet(petId: String): Boolean {
        return petStorage.removeAll { it.id == petId }
    }
}
