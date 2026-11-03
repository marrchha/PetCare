package com.example.petcare.data.remote

object SupabaseConfig {
    var SUPABASE_URL: String = ""

    var SUPABASE_ANON_KEY: String = ""

    const val TABLE_JADWAL_VAKSIN: String = "jadwal_vaksin"

    val isConfigured: Boolean
        get() = SUPABASE_URL.isNotBlank() &&
                SUPABASE_ANON_KEY.isNotBlank() &&
                SUPABASE_URL.startsWith("http")
}
