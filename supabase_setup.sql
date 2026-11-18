-- ============================================================
-- SCRIPT SETUP DATABASE & STORAGE SUPABASE - APLIKASI PETCARE
-- ============================================================

-- 1. TABEL USERS (Login, Register & Profil)
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone_number TEXT NOT NULL DEFAULT '',
    password TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. TABEL JADWAL VAKSIN (Fitur 1 - Sabitha)
CREATE TABLE IF NOT EXISTS public.jadwal_vaksin (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'user-001',
    nama_hewan TEXT NOT NULL,
    jenis TEXT NOT NULL DEFAULT 'Vaksin',
    keperluan TEXT NOT NULL,
    tanggal TEXT NOT NULL,
    jam TEXT NOT NULL,
    klinik TEXT NOT NULL,
    catatan TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'Akan Datang',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. TABEL KLINIK, DOKTER, & LAYANAN (Fitur 2 - Andien)
CREATE TABLE IF NOT EXISTS public.klinik_info (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    phone_number TEXT NOT NULL,
    operational_hours TEXT NOT NULL DEFAULT '08:00 - 20:00 WIB',
    emergency_available BOOLEAN NOT NULL DEFAULT true,
    description TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS public.dokter (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    specialization TEXT NOT NULL,
    practice_days TEXT NOT NULL,
    practice_hours TEXT NOT NULL,
    phone_contact TEXT NOT NULL,
    clinic_id TEXT NOT NULL DEFAULT 'clinic-001',
    clinic_name TEXT NOT NULL DEFAULT 'Petcare Malang',
    photo_uri TEXT,
    avatar_index INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    experience_years INT NOT NULL DEFAULT 0,
    notes TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS public.layanan_klinik (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    estimated_price BIGINT NOT NULL DEFAULT 0,
    duration_minutes INT NOT NULL DEFAULT 15,
    category TEXT NOT NULL DEFAULT 'Pemeriksaan'
);

-- 4. TABEL JADWAL MAKAN (Fitur 3 - Jeanete)
CREATE TABLE IF NOT EXISTS public.feeding_schedules (
    id TEXT PRIMARY KEY,
    pet_name TEXT NOT NULL,
    time TEXT NOT NULL,
    food_type TEXT NOT NULL,
    portion TEXT NOT NULL,
    notes TEXT NOT NULL DEFAULT ''
);

-- 5. TABEL RIWAYAT KESEHATAN (Fitur 4 - Givenia)
CREATE TABLE IF NOT EXISTS public.riwayat_kesehatan (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL DEFAULT 'user-001',
    pet_name TEXT NOT NULL,
    pet_type TEXT NOT NULL DEFAULT 'Kucing',
    jenis TEXT NOT NULL DEFAULT 'SAKIT',
    judul TEXT NOT NULL,
    tanggal TEXT NOT NULL,
    dokter_atau_klinik TEXT NOT NULL,
    catatan_medis TEXT NOT NULL,
    biaya BIGINT,
    status_pemulihan TEXT NOT NULL DEFAULT 'Selesai',
    created_at BIGINT NOT NULL DEFAULT 0
);

-- 6. TABEL DATA HEWAN (Fitur 5 - Alchalisa)
CREATE TABLE IF NOT EXISTS public.pets (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    species TEXT NOT NULL,
    breed TEXT NOT NULL,
    gender TEXT NOT NULL,
    age_months INT NOT NULL DEFAULT 0,
    weight_kg DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    color TEXT NOT NULL DEFAULT '',
    photo_uri TEXT,
    notes TEXT NOT NULL DEFAULT '',
    accent_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. MATIKAN RLS AGAR SEMUA FITUR BISA CRUD TANPA HAMBATAN
ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.jadwal_vaksin DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.klinik_info DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.dokter DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.layanan_klinik DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.feeding_schedules DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.riwayat_kesehatan DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.pets DISABLE ROW LEVEL SECURITY;

-- 8. BUAT BUCKET STORAGE UNTUK UPLOAD FOTO HEWAN & PROFIL
INSERT INTO storage.buckets (id, name, public)
VALUES ('petcare-media', 'petcare-media', true)
ON CONFLICT (id) DO UPDATE SET public = true;

DROP POLICY IF EXISTS "Public Access petcare-media" ON storage.objects;
CREATE POLICY "Public Access petcare-media"
ON storage.objects FOR ALL
USING (bucket_id = 'petcare-media')
WITH CHECK (bucket_id = 'petcare-media');

-- 9. OPSIONAL: RESET DATA LAMA YANG ID-NYA MASIH RANDOM UUID
-- Jalankan perintah di bawah ini jika ingin membersihkan data lama yang ID-nya random,
-- supaya saat aplikasi dijalankan ulang semua tabel otomatis terisi ulang dengan ID berurutan:
-- TRUNCATE TABLE public.jadwal_vaksin, public.klinik_info, public.dokter, public.layanan_klinik, public.feeding_schedules, public.riwayat_kesehatan, public.pets;
