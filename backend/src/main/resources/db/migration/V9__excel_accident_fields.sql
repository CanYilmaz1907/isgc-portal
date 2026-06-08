-- Migration V9: Add Excel-based accident tracking fields
-- Based on EASTCON TECH Kaza Takip Logu structure

-- Add new columns to accidents table
ALTER TABLE accidents 
ADD COLUMN IF NOT EXISTS area VARCHAR(50), -- Saha İçinde / Saha Dışında
ADD COLUMN IF NOT EXISTS hazard_source VARCHAR(255), -- Tehlike Kaynağı
ADD COLUMN IF NOT EXISTS injured_body_part VARCHAR(255), -- Yaralanan Vücut Bölgesi
ADD COLUMN IF NOT EXISTS injury_type VARCHAR(255), -- Yaralanma Türü
ADD COLUMN IF NOT EXISTS employee_registration_no VARCHAR(50), -- Sicil Numarası
ADD COLUMN IF NOT EXISTS supervisor_employee_id UUID REFERENCES employees(id), -- İşin Süpervizörü
ADD COLUMN IF NOT EXISTS time_period VARCHAR(50); -- Saat (0800*1200 formatı)

-- Create index for supervisor lookup
CREATE INDEX IF NOT EXISTS idx_accidents_supervisor ON accidents(supervisor_employee_id);

-- Update accident_class to support Excel values
-- Excel values: Ölüm, Kayıp Günlü Kaza, Kalıcı Sakatlık, Kısıtlı İş Görmezlik, 
-- Tıbbi Müdahale, İlk Yardım, Ucuz Atlatma, Mal Ekipman Kazası, Araç/Trafik Kazası, Çevre Kazası, Yangın
-- We'll keep the enum but add a mapping table for Excel compatibility

-- Create lookup table for Excel accident classifications
CREATE TABLE IF NOT EXISTS accident_classification_mapping (
  excel_value VARCHAR(100) PRIMARY KEY,
  system_class VARCHAR(50) NOT NULL,
  description TEXT
);

INSERT INTO accident_classification_mapping (excel_value, system_class, description)
VALUES
  ('Ölüm', 'FATAL', 'Ölümle sonuçlanan kaza'),
  ('Kayıp Günlü Kaza', 'MAJOR', 'İş günü kaybına neden olan kaza'),
  ('Kalıcı Sakatlık', 'MAJOR', 'Kalıcı sakatlığa neden olan kaza'),
  ('Kısıtlı İş Görmezlik', 'MAJOR', 'Kısıtlı iş görmezliğe neden olan kaza'),
  ('Tıbbi Müdahale', 'MINOR', 'Tıbbi müdahale gerektiren kaza'),
  ('İlk Yardım', 'MINOR', 'İlk yardım gerektiren kaza'),
  ('Ucuz Atlatma', 'NEAR_MISS', 'Ucuz atlatılan kaza'),
  ('Mal Ekipman Kazası', 'MINOR', 'Mal ve ekipman hasarına neden olan kaza'),
  ('Araç/Trafik Kazası', 'MAJOR', 'Araç veya trafik kazası'),
  ('Çevre Kazası', 'MINOR', 'Çevreye zarar veren kaza'),
  ('Yangın', 'MAJOR', 'Yangın olayı')
ON CONFLICT (excel_value) DO NOTHING;

