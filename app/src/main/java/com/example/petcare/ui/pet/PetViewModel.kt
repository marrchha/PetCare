package com.example.petcare.ui.pet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.Pet
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetUiState(
    val allPets: List<Pet> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
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

    init {
        refreshPets()
    }

    fun refreshPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchAllPets()
            result.onSuccess { pets ->
                _uiState.update { it.copy(allPets = pets, isLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(allPets = repository.getAllPets(), isLoading = false) }
            }
        }
    }

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

    fun savePet(context: Context, onSuccess: (String) -> Unit) {
        val s = _uiState.value
        if (s.isSaving) return

        val name = s.formName.trim()
        val breed = s.formBreed.trim()
        val age = s.formAgeMonths.trim().toIntOrNull()
        val weight = s.formWeightKg.trim().replace(",", ".").toDoubleOrNull()
        val color = s.formColor.trim()

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(formError = "Nama hewan wajib diisi.") }
                return
            }
            breed.isBlank() -> {
                _uiState.update { it.copy(formError = "Ras / jenis keturunan hewan wajib diisi.") }
                return
            }
            age == null || age < 0 -> {
                _uiState.update { it.copy(formError = "Masukkan umur hewan yang valid (dalam bulan).") }
                return
            }
            weight == null || weight <= 0.0 -> {
                _uiState.update { it.copy(formError = "Masukkan berat badan hewan yang valid (kg).") }
                return
            }
            color.isBlank() -> {
                _uiState.update { it.copy(formError = "Warna bulu / ciri fisik wajib diisi.") }
                return
            }
        }

        val editingId = s.editingPetId
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, formError = null) }

            val result = if (editingId == null) {
                repository.insertPetRemote(
                    context = context,
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
                repository.updatePetRemote(context, updated)
            }

            result.fold(
                onSuccess = { savedPet ->
                    _uiState.update {
                        it.copy(
                            allPets = repository.getAllPets(),
                            isSaving = false,
                            formError = null
                        )
                    }
                    onSuccess(savedPet.id)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            formError = err.message ?: "Gagal menyimpan data hewan ke Supabase."
                        )
                    }
                }
            )
        }
    }

    fun deletePet(petId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePetRemote(petId)
            _uiState.update { it.copy(allPets = repository.getAllPets()) }
            onSuccess()
        }
    }
}
