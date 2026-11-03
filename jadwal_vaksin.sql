-- ==============================================================
-- SKRIP SQL DDL SUPABASE: TABEL JADWAL VAKSIN & KONTROL
-- Penanggung Jawab: Sabitha Marsyarira Nayla (NIM: 245150401111032)
-- Fitur 1: Jadwal Vaksin/Kontrol
-- ==============================================================

-- 1. Buat Tabel jadwal_vaksin
CREATE TABLE IF NOT EXISTS public.jadwal_vaksin (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id TEXT NOT NULL,
    nama_hewan TEXT NOT NULL,
    jenis TEXT NOT NULL CHECK (jenis IN ('Vaksin', 'Kontrol')),
    keperluan TEXT NOT NULL,
    tanggal TEXT NOT NULL,
    jam TEXT NOT NULL,
    klinik TEXT NOT NULL,
    catatan TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'Akan Datang' CHECK (status IN ('Akan Datang', 'Selesai')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Aktifkan Row Level Security (RLS)
ALTER TABLE public.jadwal_vaksin ENABLE ROW LEVEL SECURITY;

-- 3. Kebijakan Akses (Policy) CRUD Publik/Anon (atau Sesuaikan dengan auth.uid())
DROP POLICY IF EXISTS "Akses CRUD Jadwal Vaksin Publik" ON public.jadwal_vaksin;
CREATE POLICY "Akses CRUD Jadwal Vaksin Publik" ON public.jadwal_vaksin
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- 4. Data Sampel Awal (Opsional)
INSERT INTO public.jadwal_vaksin (id, user_id, nama_hewan, jenis, keperluan, tanggal, jam, klinik, catatan, status)
VALUES
    ('jv-001', 'user-demo-001', 'Mimi (Kucing)', 'Vaksin', 'Vaksin Tricat & Rabies', '2026-11-12', '09:30', 'Klinik Satwa Sehat Husada', 'Bawa buku vaksin sebelumnya. Hewan harus dalam keadaan sehat dan nafsu makan normal.', 'Akan Datang'),
    ('jv-002', 'user-demo-001', 'Rocky (Anjing)', 'Kontrol', 'Pemeriksaan Rutin & Cek Gigi', '2026-11-18', '14:00', 'Pet Care Center & Hospital', 'Pemeriksaan plak pada taring kanan atas dan konsultasi vitamin bulu.', 'Akan Datang'),
    ('jv-003', 'user-demo-001', 'Chiki (Kelinci)', 'Kontrol', 'Kontrol Pasca Pengobatan Telinga', '2026-10-22', '10:30', 'Veterinary Holistic Care', 'Telinga sudah bersih dari earmites, obat tetes dihentikan sesuai instruksi dokter.', 'Selesai'),
    ('jv-004', 'user-demo-001', 'Milo (Kucing)', 'Vaksin', 'Vaksin FeLV Booster', '2026-10-15', '11:00', 'Klinik Satwa Sehat Husada', 'Sudah disuntik vaksin booster, tidak ada tanda-tanda alergi atau lemas.', 'Selesai')
ON CONFLICT (id) DO NOTHING;
