package com.example.petcare.data.remote

/**
 * Konfigurasi koneksi Supabase untuk aplikasi PetCare.
 * Pengguna dapat memasukkan URL proyek dan Anon Key Supabase di sini.
 */
object SupabaseConfig {
    // Ganti dengan URL Project Supabase Anda, misal: "https://xyzcompany.supabase.co"
    var SUPABASE_URL: String = ""

    // Ganti dengan Project Anon Public Key dari Supabase Project Settings -> API
    var SUPABASE_ANON_KEY: String = ""

    const val TABLE_JADWAL_VAKSIN: String = "jadwal_vaksin"

    /**
     * Memeriksa apakah konfigurasi Supabase sudah terisi dengan kredensial yang valid.
     */
    val isConfigured: Boolean
        get() = SUPABASE_URL.isNotBlank() &&
                SUPABASE_ANON_KEY.isNotBlank() &&
                SUPABASE_URL.startsWith("http")
}
