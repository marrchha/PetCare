package com.example.petcare.data.repository

import com.example.petcare.data.model.FeedingSchedule
import com.example.petcare.data.remote.SequentialIdGenerator
import com.example.petcare.data.remote.SupabaseConfig
import com.example.petcare.data.remote.SupabaseRestClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Repository untuk Fitur 3: Jadwal Makan Harian (Jeanete Arthika Lorentz R.).
 * Terintegrasi dengan tabel Supabase `feeding_schedules` (Read, Insert, Update, Delete)
 * dengan ID berurutan (feed-001, feed-002, dst.) dan berelasi dengan data Hewan (Fitur 5).
 */
class FeedingRepository {

    companion object {
        private const val TABLE_FEEDING = "feeding_schedules"

        private var dataServer = listOf(
            FeedingSchedule("feed-001", "Mochi", "07:00", "Dry food Royal Canin", "50 gram", "Campur sedikit air hangat"),
            FeedingSchedule("feed-002", "Mochi", "18:00", "Wet food", "1 sachet (85 gram)", ""),
            FeedingSchedule("feed-003", "Buddy", "08:00", "Dog food adult", "150 gram", "Jangan diberi tulang"),
            FeedingSchedule("feed-004", "Luna", "17:30", "Dry food British Shorthair", "60 gram", "Beri minum segar setelah makan")
        )
    }

    private val _schedules = MutableStateFlow<List<FeedingSchedule>>(dataServer)
    val schedules: StateFlow<List<FeedingSchedule>> = _schedules.asStateFlow()

    suspend fun load() {
        if (SupabaseConfig.isConfigured) {
            val remoteRes = SupabaseRestClient.getList(
                table = TABLE_FEEDING,
                queryParams = "select=*&order=id.asc"
            )
            remoteRes.onSuccess { arr ->
                if (arr.length() == 0 && dataServer.isNotEmpty()) {
                    dataServer.forEach { seed ->
                        SupabaseRestClient.insert(TABLE_FEEDING, feedingToJson(seed))
                    }
                } else if (arr.length() > 0) {
                    val loaded = mutableListOf<FeedingSchedule>()
                    for (i in 0 until arr.length()) {
                        loaded.add(jsonToFeeding(arr.getJSONObject(i), i))
                    }
                    loaded.sortBy { it.id }
                    dataServer = loaded
                }
            }
        }
        _schedules.value = dataServer
    }

    suspend fun add(
        petName: String,
        time: String,
        foodType: String,
        portion: String,
        notes: String
    ): Result<FeedingSchedule> {
        val nextSequentialId = SequentialIdGenerator.nextId(
            table = TABLE_FEEDING,
            prefix = "feed",
            localIds = dataServer.map { it.id }
        )
        val baru = FeedingSchedule(
            id = nextSequentialId,
            petName = petName.trim(),
            time = time,
            foodType = foodType.trim(),
            portion = portion.trim(),
            notes = notes.trim()
        )
        if (SupabaseConfig.isConfigured) {
            SupabaseRestClient.insert(
                table = TABLE_FEEDING,
                payload = feedingToJson(baru)
            ).onFailure { err ->
                return Result.failure(err)
            }
        }
        dataServer = (dataServer + baru).sortedBy { it.id }
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
        val updated = FeedingSchedule(
            id = id,
            petName = petName.trim(),
            time = time,
            foodType = foodType.trim(),
            portion = portion.trim(),
            notes = notes.trim()
        )
        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(id)
            SupabaseRestClient.update(
                table = TABLE_FEEDING,
                matchQuery = "id=eq.$encodedId",
                payload = feedingToJson(updated)
            ).onFailure { err ->
                return Result.failure(err)
            }
        }
        dataServer = dataServer.map { if (it.id == id) updated else it }
        _schedules.value = dataServer
        return Result.success(updated)
    }

    suspend fun delete(id: String): Result<Unit> {
        if (SupabaseConfig.isConfigured) {
            val encodedId = SupabaseRestClient.encodeParam(id)
            SupabaseRestClient.delete(
                table = TABLE_FEEDING,
                matchQuery = "id=eq.$encodedId"
            ).onFailure { err ->
                return Result.failure(err)
            }
        }
        dataServer = dataServer.filterNot { it.id == id }
        _schedules.value = dataServer
        return Result.success(Unit)
    }

    private fun feedingToJson(item: FeedingSchedule): JSONObject = JSONObject().apply {
        put("id", item.id)
        put("pet_name", item.petName)
        put("time", item.time)
        put("food_type", item.foodType)
        put("portion", item.portion)
        put("notes", item.notes)
    }

    private fun jsonToFeeding(obj: JSONObject, indexFallback: Int): FeedingSchedule = FeedingSchedule(
        id = obj.optString("id", String.format("feed-%03d", indexFallback + 1)),
        petName = obj.optString("pet_name", ""),
        time = obj.optString("time", ""),
        foodType = obj.optString("food_type", ""),
        portion = obj.optString("portion", ""),
        notes = obj.optString("notes", "")
    )
}
