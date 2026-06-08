-- Migration V10: Add new fields based on new accident report template
-- Based on _kaza raporu SABLON.pdf and 32-Tıbbi müdahale kaza raporu(MTC).pdf

-- Add new columns to accidents table
ALTER TABLE accidents 
ADD COLUMN IF NOT EXISTS group_company_name VARCHAR(255), -- Grup Şirket Adı
ADD COLUMN IF NOT EXISTS responsible_person VARCHAR(255), -- Sorumlu
ADD COLUMN IF NOT EXISTS estimated_cost VARCHAR(100), -- Tahmini Maliyet
ADD COLUMN IF NOT EXISTS work_related BOOLEAN DEFAULT TRUE, -- İşle İlgili / İşle İlgisiz
ADD COLUMN IF NOT EXISTS work_during_accident VARCHAR(500), -- Kaza/Olay Esnasında Yapılan İş
ADD COLUMN IF NOT EXISTS injured_person_age INTEGER, -- Kazazedenin Yaşı
ADD COLUMN IF NOT EXISTS injured_person_profession VARCHAR(255), -- Meslek
ADD COLUMN IF NOT EXISTS injured_person_gender VARCHAR(20), -- Cinsiyet (Erkek/Kadın)
ADD COLUMN IF NOT EXISTS injured_person_nationality VARCHAR(100), -- Milliyet
ADD COLUMN IF NOT EXISTS injured_person_company VARCHAR(255), -- Çalıştığı Firma
ADD COLUMN IF NOT EXISTS actions_taken JSONB DEFAULT '[]'::jsonb, -- Alınmış/Alınacak Aksiyonlar
ADD COLUMN IF NOT EXISTS prepared_by_user_id UUID REFERENCES users(id), -- Raporu Hazırlayan
ADD COLUMN IF NOT EXISTS prepared_at TIMESTAMPTZ; -- Hazırlanma Tarihi

-- Create index for prepared_by lookup
CREATE INDEX IF NOT EXISTS idx_accidents_prepared_by ON accidents(prepared_by_user_id);

