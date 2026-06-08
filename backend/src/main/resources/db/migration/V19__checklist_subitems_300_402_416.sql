-- Denetim alt soruları: kategori 300, 402–416
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

  -- 300 Çevre Yönetimi (ağırlık 1)
  items := '[
    {"no":3001,"q":"Atık ayrıştırma ve bertaraf prosedürleri uygulanıyor mu?"},
    {"no":3002,"q":"Sızıntı/dökülme önleme ve müdahale ekipmanları hazır mı?"},
    {"no":3003,"q":"Çevresel izin ve raporlama yükümlülükleri takip ediliyor mu?"}
  ]'::JSON;
  cat := 300; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 402 Yüksekte Çalışma (ağırlık 3)
  items := '[
    {"no":4021,"q":"Yüksekte çalışma risk değerlendirmesi yapılmış mı?"},
    {"no":4022,"q":"Emniyet kemeri/yaşam hattı doğru kullanılıyor mu?"},
    {"no":4023,"q":"Kenar korumaları ve açıklıklar güvenli mi?"},
    {"no":4024,"q":"Düşme önleme eğitimleri güncel mi?"}
  ]'::JSON;
  cat := 402; w := 0.75;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 403 Acil Durum (ağırlık 2)
  items := '[
    {"no":4031,"q":"Acil durum planları güncel ve erişilebilir mi?"},
    {"no":4032,"q":"Tahliye tatbikatları periyodik yapılıyor mu?"},
    {"no":4033,"q":"Acil durum ekipleri ve iletişim listeleri mevcut mu?"}
  ]'::JSON;
  cat := 403; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 404 Tertip Düzen & Depolama (ağırlık 1)
  items := '[
    {"no":4041,"q":"Geçiş yolları ve acil çıkışlar açık mı?"},
    {"no":4042,"q":"Malzemeler düzenli istiflenmiş ve etiketli mi?"},
    {"no":4043,"q":"Kaygan/kirli zeminler kontrol altında mı?"}
  ]'::JSON;
  cat := 404; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 405 Kazılar (ağırlık 1)
  items := '[
    {"no":4051,"q":"Kazı izinleri alınmış ve sahada görünür mü?"},
    {"no":4052,"q":"Shoring/banketleme gerektiğinde uygulanıyor mu?"},
    {"no":4053,"q":"Yer altı hatları tespit edilmiş mi?"}
  ]'::JSON;
  cat := 405; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 406 Elektrik Güvenliği (ağırlık 3)
  items := '[
    {"no":4061,"q":"Geçici elektrik tesisatı standartlara uygun mu?"},
    {"no":4062,"q":"Topraklama/RCD kontrolleri yapılıyor mu?"},
    {"no":4063,"q":"Kilitli-etiketli (LOTO) prosedürü uygulanıyor mu?"},
    {"no":4064,"q":"Kablo hasarları ve açık uçlar kontrol altında mı?"}
  ]'::JSON;
  cat := 406; w := 0.75;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 407 El Aletleri (ağırlık 2)
  items := '[
    {"no":4071,"q":"Aletler periyodik kontrolden geçiyor mu?"},
    {"no":4072,"q":"Koruyucu kılıflar ve guardlar yerinde mi?"},
    {"no":4073,"q":"Yanlış alet kullanımı gözlemleniyor mu?"}
  ]'::JSON;
  cat := 407; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 408 KKE (ağırlık 2)
  items := '[
    {"no":4081,"q":"KKE matrisi tanımlı ve uygulanıyor mu?"},
    {"no":4082,"q":"KKE kullanımı saha gözlemlerinde uygun mu?"},
    {"no":4083,"q":"KKE stokları ve değişim kayıtları yeterli mi?"}
  ]'::JSON;
  cat := 408; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 409 Araçlar & Mobil Ekipman (ağırlık 2)
  items := '[
    {"no":4091,"q":"Operatör yetkinlikleri ve ehliyetler geçerli mi?"},
    {"no":4092,"q":"Günlük ön kullanım kontrolleri yapılıyor mu?"},
    {"no":4093,"q":"Yaya-araç ayrımı ve hız limitleri uygulanıyor mu?"}
  ]'::JSON;
  cat := 409; w := 0.67;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 410 Kapalı Alanlar (ağırlık 1)
  items := '[
    {"no":4101,"q":"Kapalı alan giriş izinleri uygulanıyor mu?"},
    {"no":4102,"q":"Gaz ölçümü ve havalandırma yapılıyor mu?"},
    {"no":4103,"q":"Gözetmen ve kurtarma planı hazır mı?"}
  ]'::JSON;
  cat := 410; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 411 Yangın Güvenliği (ağırlık 3)
  items := '[
    {"no":4111,"q":"Yangın söndürücüler erişilebilir ve kontrol edilmiş mi?"},
    {"no":4112,"q":"Yanıcı malzeme depolama uygun mu?"},
    {"no":4113,"q":"Hot work izinleri uygulanıyor mu?"},
    {"no":4114,"q":"Yangın alarm ve tahliye yolları işaretli mi?"}
  ]'::JSON;
  cat := 411; w := 0.75;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 412 İskeleler (ağırlık 3)
  items := '[
    {"no":4121,"q":"İskele kurulum/tahliye yetkili personel tarafından mı?"},
    {"no":4122,"q":"Etiketleme ve haftalık kontrol kayıtları mevcut mu?"},
    {"no":4123,"q":"Korkuluk, platform ve erişim güvenli mi?"},
    {"no":4124,"q":"Yük kapasitesi aşılmıyor mu?"}
  ]'::JSON;
  cat := 412; w := 0.75;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 413 MEWP (ağırlık 3)
  items := '[
    {"no":4131,"q":"Operatör eğitimleri ve yetkinlikleri geçerli mi?"},
    {"no":4132,"q":"Emniyet kemeri bağlantı noktaları kullanılıyor mu?"},
    {"no":4133,"q":"Zemin eğimi ve rüzgar limitleri kontrol ediliyor mu?"}
  ]'::JSON;
  cat := 413; w := 1.0;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 414 İş İzni (ağırlık 1)
  items := '[
    {"no":4141,"q":"PTW sistemi tanımlı ve uygulanıyor mu?"},
    {"no":4142,"q":"İş izinleri sahada görünür ve imzalı mı?"},
    {"no":4143,"q":"İzinsiz yüksek riskli işler tespit edildi mi?"}
  ]'::JSON;
  cat := 414; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 415 Basınçlı Gaz Tüpleri (ağırlık 1)
  items := '[
    {"no":4151,"q":"Tüpler dik ve sabitlenmiş durumda mı?"},
    {"no":4152,"q":"Valf koruyucuları ve kapaklar takılı mı?"},
    {"no":4153,"q":"Yanıcı/Oksijen ayrımı uygun mesafede mi?"}
  ]'::JSON;
  cat := 415; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;

  -- 416 Tehlikeli Maddeler (ağırlık 1)
  items := '[
    {"no":4161,"q":"SDS/MSDS erişilebilir ve güncel mi?"},
    {"no":4162,"q":"Kimyasal depolama uygun kabin/zemin üzerinde mi?"},
    {"no":4163,"q":"Dökülme setleri ve ikincil containment mevcut mu?"}
  ]'::JSON;
  cat := 416; w := 0.33;
  FOR rec IN SELECT * FROM json_array_elements(items) LOOP
    INSERT INTO checklist_items (id, checklist_id, item_no, question, weight, max_score, enabled, category_no, created_at, updated_at)
    VALUES (gen_random_uuid(), cid, (rec->>'no')::int, rec->>'q', w, 100.0, TRUE, cat, now(), now())
    ON CONFLICT (checklist_id, item_no) DO NOTHING;
  END LOOP;
END $$;
