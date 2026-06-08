-- Kategori gruplama ve alt sorular (örnek: kategori 100)
ALTER TABLE checklist_items
  ADD COLUMN IF NOT EXISTS category_no INT;

UPDATE checklist_items
SET category_no = item_no
WHERE category_no IS NULL;

-- Kategori 100 alt soruları (Kurumsal İSG-Ç Sistem Gereklilikleri)
DO $$
DECLARE
  cid UUID;
  rec JSON;
  items JSON := '[
    {"no":101,"q":"İSG politikası ve hedefler tanımlı ve iletilmiş mi?"},
    {"no":102,"q":"Roller, sorumluluklar ve yetkiler belirlenmiş mi?"},
    {"no":103,"q":"Yasal gereklilikler takip ediliyor mu?"},
    {"no":104,"q":"Eğitim matrisi ve kayıtları güncel mi?"},
    {"no":105,"q":"Olay/kaza bildirim prosedürü uygulanıyor mu?"},
    {"no":106,"q":"Yönetim gözden geçirmesi yapılıyor mu?"},
    {"no":107,"q":"Doküman kontrolü ve revizyon takibi var mı?"}
  ]'::JSON;
BEGIN
  SELECT id INTO cid FROM checklists WHERE code = 'IKT_HSE_AUDIT' LIMIT 1;
  IF cid IS NULL THEN RETURN; END IF;

  FOR rec IN SELECT * FROM json_array_elements(items)
  LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (
      gen_random_uuid(),
      cid,
      (rec->>'no')::int,
      rec->>'q',
      0.29,
      100.0,
      TRUE,
      100,
      now(),
      now()
    )
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;
END $$;
