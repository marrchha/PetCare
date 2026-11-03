package com.example.petcare.ui.pet

import androidx.lifecycle.ViewModel
import com.example.petcare.data.model.Pet
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PetUiState(
    val allPets: List<Pet> = emptyList(),
    val searchQuery: String = "",
    val selectedSpeciesFilter: String = "Semua",
    val editingPetId: String? = null,
    val formName: String = "",
    val formSpecies: String = "Kucing",
    val formBreed: String = "",
    val formGender: String = "Betina",
    val formAgeMonths: String = "",
    val formWeightKg: String = "",
    val formColor: String = "",
    val formPhotoUri: String? = null,
    val formNotes: String = "",
    val formError: String? = null
) {

    val filteredPets: List<Pet>
        get() = allPets.filter { pet ->
            val matchesSearch = searchQuery.isBlank() ||
                pet.name.contains(searchQuery.trim(), ignoreCase = true) ||
                pet.breed.contains(searchQuery.trim(), ignoreCase = true)
            val matchesSpecies = selectedSpeciesFilter == "Semua" ||
                pet.species.equals(selectedSpeciesFilter, ignoreCase = true)
            matchesSearch && matchesSpecies
        }
}

class PetViewModel(
    private val repository: PetRepository = PetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PetUiState(allPets = repository.getAllPets())
    )
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSpeciesFilterChange(species: String) {
        _uiState.update { it.copy(selectedSpeciesFilter = species) }
    }

    fun getPetById(petId: String): Pet? {
        return _uiState.value.allPets.find { it.id == petId } ?: repository.getPetById(petId)
    }

    fun prepareForm(petId: String?) {
        if (petId == null) {
            _uiState.update {
                it.copy(
                    editingPetId = null,
                    formName = "",
                    formSpecies = "Kucing",
                    formBreed = "",
                    formGender = "Betina",
                    formAgeMonths = "",
                    formWeightKg = "",
                    formColor = "",
                    formPhotoUri = null,
                    formNotes = "",
                    formError = null
                )
            }
        } else {
            val pet = getPetById(petId) ?: return
            _uiState.update {
                it.copy(
                    editingPetId = pet.id,
                    formName = pet.name,
                    formSpecies = pet.species,
                    formBreed = pet.breed,
                    formGender = pet.gender,
                    formAgeMonths = pet.ageMonths.toString(),
                    formWeightKg = pet.weightKg.toString(),
                    formColor = pet.color,
                    formPhotoUri = pet.photoUri,
                    formNotes = pet.notes,
                    formError = null
                )
            }
        }
    }

    fun onFormNameChange(value: String) {
        _uiState.update { it.copy(formName = value, formError = null) }
    }

    fun onFormSpeciesChange(value: String) {
        _uiState.update { it.copy(formSpecies = value, formError = null) }
    }

    fun onFormBreedChange(value: String) {
        _uiState.update { it.copy(formBreed = value, formError = null) }
    }

    fun onFormGenderChange(value: String) {
        _uiState.update { it.copy(formGender = value, formError = null) }
    }

    fun onFormAgeMonthsChange(value: String) {
        _uiState.update { it.copy(formAgeMonths = value, formError = null) }
    }

    fun onFormWeightKgChange(value: String) {
        _uiState.update { it.copy(formWeightKg = value, formError = null) }
    }

    fun onFormColorChange(value: String) {
        _uiState.update { it.copy(formColor = value, formError = null) }
    }

    fun onFormPhotoUriChange(uri: String?) {
        _uiState.update { it.copy(formPhotoUri = uri, formError = null) }
    }

    fun onFormNotesChange(value: String) {
        _uiState.update { it.copy(formNotes = value, formError = null) }
    }

    fun savePet(): String? {
        val s = _uiState.value
        val name = s.formName.trim()
        val breed = s.formBreed.trim()
        val age = s.formAgeMonths.trim().toIntOrNull()
        val weight = s.formWeightKg.trim().replace(",", ".").toDoubleOrNull()
        val color = s.formColor.trim()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formError = "Nama hewan wajib diisi.") }
                return null
            }
            breed.isBlank() -> {
                _uiState.update { it.copy(formError = "Ras / jenis keturunan hewan wajib diisi.") }
                return null
            }
            age == null || age < 0 -> {
                _uiState.update { it.copy(formError = "Masukkan umur hewan yang valid (dalam bulan).") }
                return null
            }
            weight == null || weight <= 0.0 -> {
                _uiState.update { it.copy(formError = "Masukkan berat badan hewan yang valid (kg).") }
                return null
            }
            color.isBlank() -> {
                _uiState.update { it.copy(formError = "Warna bulu / ciri fisik wajib diisi.") }
                return null
            }
        }

        val editingId = s.editingPetId
        return if (editingId == null) {
            val created = repository.insertPet(
                name = name,
                species = s.formSpecies,
                breed = breed,
                gender = s.formGender,
                ageMonths = age,
                weightKg = weight,
                color = color,
                photoUri = s.formPhotoUri,
                notes = s.formNotes
            )
            _uiState.update { it.copy(allPets = repository.getAllPets(), formError = null) }
            created.id
        } else {
            val existing = getPetById(editingId)
            val updated = Pet(
                id = editingId,
                name = name,
                species = s.formSpecies,
                breed = breed,
                gender = s.formGender,
                ageMonths = age,
                weightKg = weight,
                color = color,
                photoUri = s.formPhotoUri,
                notes = s.formNotes.trim(),
                accentIndex = existing?.accentIndex ?: 0
            )
            repository.updatePet(updated)
            _uiState.update { it.copy(allPets = repository.getAllPets(), formError = null) }
            editingId
        }
    }

    fun deletePet(petId: String) {
        repository.deletePet(petId)
        _uiState.update { it.copy(allPets = repository.getAllPets()) }
    }
}
