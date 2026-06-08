-- Denetim alt soruları: kategori 201, 202, 203, 401
DO $$
DECLARE
  cid UUID;
  rec JSON;
  cat INT;
  w NUMERIC;
  items JSON;
BEGIN
  SELECT id INTO cid FROM checklists WHERE code = 'IKT_HSE_AUDIT' LIMIT 1;
  IF cid IS NULL THEN RETURN; END IF;

  -- 201 Medikal
  items := '[
    {"no":2011,"q":"Revir/ilk yardım odası uygun ve erişilebilir mi?"},
    {"no":2012,"q":"İlaç ve malzeme stokları güncel mi?"},
    {"no":2013,"q":"Sağlık taramaları ve kayıtlar mevcut mu?"}
  ]'::JSON;
  cat := 201; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 202 Yatakhane
  items := '[
    {"no":2021,"q":"Yatakhane hijyen ve havalandırma uygun mu?"},
    {"no":2022,"q":"Acil çıkışlar işaretli ve açık mı?"},
    {"no":2023,"q":"Yangın söndürme ekipmanları yerinde mi?"}
  ]'::JSON;
  cat := 202; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 203 Mutfak
  items := '[
    {"no":2031,"q":"Gıda güvenliği ve sıcaklık kontrolleri yapılıyor mu?"},
    {"no":2032,"q":"Personel hijyen kurallarına uyuluyor mu?"},
    {"no":2033,"q":"Temizlik ve haşere kontrolü kayıtları mevcut mu?"}
  ]'::JSON;
  cat := 203; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 401 Kaldırma
  items := '[
    {"no":4011,"q":"Kaldırma planları hazır ve uygulanıyor mu?"},
    {"no":4012,"q":"Sapan ve halatlar periyodik kontrol ediliyor mu?"},
    {"no":4013,"q":"Vinç operatörleri yetkili ve eğitimli mi?"},
    {"no":4014,"q":"Kaldırma alanı güvenlik bariyerleri ile ayrılmış mı?"}
  ]'::JSON;
  cat := 401; w := 0.75;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;
END $$;
