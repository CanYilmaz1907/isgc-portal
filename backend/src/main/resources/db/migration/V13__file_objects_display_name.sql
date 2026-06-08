-- Fotoğraf/dosya için manuel girilen görünen isim (yükleme sonrası düzenlenebilir)
ALTER TABLE file_objects ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
