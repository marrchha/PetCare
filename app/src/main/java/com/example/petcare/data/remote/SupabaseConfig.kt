package com.example.petcare.data.remote

object SupabaseConfig {
    const val SUPABASE_URL = "https://anbszzxzomglealyklue.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFuYnN6enh6b21nbGVhbHlrbHVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTQyOTU4MTYsImV4cCI6MjEwOTg3MTgxNn0.646ZANBfBK52rdm3Pcn6hCtbalE9x0O8EGfXs8o5jFE"
    const val STORAGE_BUCKET = "petcare-media"

    val isConfigured: Boolean
        get() = SUPABASE_URL.startsWith("https://") && SUPABASE_ANON_KEY.isNotBlank()
}
