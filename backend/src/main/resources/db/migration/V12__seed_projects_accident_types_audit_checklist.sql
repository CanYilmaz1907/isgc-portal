-- V12: Örnek CSV dosyalarından projeler, kaza türleri ve denetim maddeleri
-- Kaynak: HSE-Statistics-ECT, Kaza-Kayıt-Formu, EASTCON-TECH-Kaza-Takip-Logu, АУДИТ-октябрь-2025_Озерный

-- ========== PROJELER (HSE-Statistics + Kaza formları + Audit) ==========
INSERT INTO projects (id, code, name, enabled, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'OZGOK', 'OZGOK (ОЗГОК)', TRUE, now(), now()),
  (gen_random_uuid(), 'CHITA', 'CHITA – Central Office (Чита база)', TRUE, now(), now()),
  (gen_random_uuid(), 'KULTUMA', 'KULTUMA (Култума)', TRUE, now(), now()),
  (gen_random_uuid(), 'ERM', 'ERM (Ермаковское)', TRUE, now(), now()),
  (gen_random_uuid(), 'VEDUGA', 'Veduga (Ведуга)', TRUE, now(), now()),
  (gen_random_uuid(), 'AK_SUG', 'Ak-Sug (Ак-Суг)', TRUE, now(), now()),
  (gen_random_uuid(), 'KUMROCH', 'Kumroch (ДВ Кумроч)', TRUE, now(), now()),
  (gen_random_uuid(), 'AFS', 'AFS (АФС)', TRUE, now(), now()),
  (gen_random_uuid(), 'OZ_MINING_PLANT', 'OZ Mining Plant', TRUE, now(), now()),
  (gen_random_uuid(), 'OZERNY', 'ГОК Озерный (Ozerny)', TRUE, now(), now())
ON CONFLICT (code) DO NOTHING;

-- ========== KAZA TÜRLERİ (Kaza Kayıt Formu / EASTCON Kaza Takip Logu) ==========
INSERT INTO accident_types (id, code, name, form_schema, enabled, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'MAL_EKIPMAN_KAZASI', 'Mal / Ekipman Kazası', '{"version":1,"title":"Mal / Ekipman Kazası","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'TIBBI_MUDAHALE', 'Tıbbi Müdahaleli Kaza', '{"version":1,"title":"Tıbbi Müdahaleli Kaza","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'UCUZ_ATLATMA', 'Ucuz Atlatma', '{"version":1,"title":"Ucuz Atlatma","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'ILK_YARDIM', 'İlk Yardımlı Kaza', '{"version":1,"title":"İlk Yardımlı Kaza","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'YANGIN', 'Yangın', '{"version":1,"title":"Yangın","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'TEHLIKELI_DURUM', 'Tehlikeli Durum', '{"version":1,"title":"Tehlikeli Durum","fields":[]}'::jsonb, TRUE, now(), now()),
  (gen_random_uuid(), 'MOTORLU_ARAC_KAZASI', 'Motorlu Araç Kazası', '{"version":1,"title":"Motorlu Araç Kazası","fields":[]}'::jsonb, TRUE, now(), now())
ON CONFLICT (code) DO NOTHING;

-- ========== DENETİM CHECKLİSTİ (АУДИТ Озерный – IKT HSE Audit maddeleri) ==========
INSERT INTO checklists (id, code, title, scope, enabled, created_at, updated_at)
SELECT gen_random_uuid(), 'IKT_HSE_AUDIT', 'Site HSE Audit / İSG-Ç Denetimi / Аудит в области ОТ, ТБ и ООС', 'GENERAL', TRUE, now(), now()
WHERE NOT EXISTS (SELECT 1 FROM checklists WHERE code = 'IKT_HSE_AUDIT');

-- Checklist item'ları (IKT HSE Audit maddeleri – ağırlıklar: 401,402,406,411,412,413=3; 100,201,202,203,403,407,408,409=2; diğer=1)
DO $$
DECLARE
  cid UUID;
  rec JSON;
  w NUMERIC;
  items JSON := '[
    {"no":100,"q":"Corporate HSE System Requirements / Kurumsal İSG-Ç Sistem Gereklilikleri"},
    {"no":201,"q":"Medical Services and Facilities / Medikal Hizmet ve Tesisleri"},
    {"no":202,"q":"Dormitories and Facilities / Yatakhane ve Servis Binaları"},
    {"no":203,"q":"Kitchen and Mess Hall / Mutfak ve Yemekhane"},
    {"no":300,"q":"Environmental Management / Çevre Yönetimi"},
    {"no":401,"q":"Lifting & Rigging / Kaldırma Operasyonları"},
    {"no":402,"q":"Work at Height / Yüksekte Çalışmalar"},
    {"no":403,"q":"Emergency Preparedness / Acil Durumlar"},
    {"no":404,"q":"Housekeeping & Storage / Tertip Düzen & Depolama"},
    {"no":405,"q":"Excavations / Kazılar"},
    {"no":406,"q":"Electrical Safety / Elektrik Güvenliği"},
    {"no":407,"q":"Hand & Power Tools / El Aletleri & Elektrikli Aletler"},
    {"no":408,"q":"Personal Protective Equipment / Kişisel Koruyucu Ekipmanlar"},
    {"no":409,"q":"Vehicles & Mobile Equipment / Araçlar & Mobil Ekipmanlar"},
    {"no":410,"q":"Confined Spaces / Kapalı Alanlar"},
    {"no":411,"q":"Fire Safety / Yangın Güvenliği"},
    {"no":412,"q":"Scaffolding / İskeleler"},
    {"no":413,"q":"Mobile Elevating Work Platforms (MEWP) / Mobil Yükselen Çalışma Platformları"},
    {"no":414,"q":"Permit to Work (PTW) / İş İzni"},
    {"no":415,"q":"Compressed Gas Cylinders / Basınçlı Gaz Tüpleri"},
    {"no":416,"q":"Hazardous Materials / Tehlikeli Maddeler"}
  ]'::JSON;
BEGIN
  SELECT id INTO cid FROM checklists WHERE code = 'IKT_HSE_AUDIT' LIMIT 1;
  IF cid IS NULL THEN RETURN; END IF;

  FOR rec IN SELECT * FROM json_array_elements(items)
  LOOP
    w := 1.0;
    IF (rec->>'no')::int IN (401,402,406,411,412,413) THEN w := 3.0; END IF;
    IF (rec->>'no')::int IN (100,201,202,203,403,407,408,409) THEN w := 2.0; END IF;
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;
END $$;
