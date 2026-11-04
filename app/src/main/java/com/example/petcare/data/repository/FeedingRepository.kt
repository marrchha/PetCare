package com.example.petcare.data.repository

import com.example.petcare.data.model.FeedingSchedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository untuk Fitur 3: Jadwal Makan Harian.
 * Pemilik utama data jadwal makan (Single Source of Truth).
 * Menggunakan data simulasi in-memory dengan penundaan coroutine (network delay).
 */
class FeedingRepository {

    private var dataServer = listOf(
        FeedingSchedule("feed-001", "Mochi", "07:00", "Dry food Royal Canin", "50 gram", "Campur sedikit air hangat"),
        FeedingSchedule("feed-002", "Mochi", "18:00", "Wet food", "1 sachet (85 gram)", ""),
        FeedingSchedule("feed-003", "Bruno", "08:00", "Dog food adult", "150 gram", "Jangan diberi tulang"),
        FeedingSchedule("feed-004", "Bruno", "17:30", "Dog food adult", "150 gram", "Beri minum setelah makan")
    )
    private var nomorBerikutnya = 5

    private val _schedules = MutableStateFlow<List<FeedingSchedule>>(emptyList())
    val schedules: StateFlow<List<FeedingSchedule>> = _schedules.asStateFlow()

    suspend fun load() {
        delay(800)
        _schedules.value = dataServer
    }

    suspend fun add(
        petName: String,
        time: String,
        foodType: String,
        portion: String,
        notes: String
    ): Result<FeedingSchedule> {
        delay(500)
        val baru = FeedingSchedule(
            id = "feed-%03d".format(nomorBerikutnya++),
            petName = petName.trim(),
            time = time,
            foodType = foodType.trim(),
            portion = portion.trim(),
            notes = notes.trim()
        )
        dataServer = dataServer + baru
        _schedules.value = dataServer
        return Result.success(baru)
    }

    suspend fun update(
        id: String,
        petName: String,
        time: String,
        foodType: String,
        portion: String,
        notes: String
    ): Result<FeedingSchedule> {
        delay(500)
        var updatedSchedule: FeedingSchedule? = null
        dataServer = dataServer.map {
            if (it.id == id) {
                val updated = it.copy(
                    petName = petName.trim(),
                    time = time,
                    foodType = foodType.trim(),
                    portion = portion.trim(),
                    notes = notes.trim()
                )
                updatedSchedule = updated
                updated
            } else {
                it
            }
        }
        _schedules.value = dataServer
        return updatedSchedule?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Jadwal makan tidak ditemukan"))
    }

    suspend fun delete(id: String): Result<Unit> {
        delay(300)
        dataServer = dataServer.filterNot { it.id == id }
        _schedules.value = dataServer
        return Result.success(Unit)
    }
}
