-- Seed: accident types with example dynamic form schemas
INSERT INTO accident_types (id, code, name, form_schema, enabled)
VALUES
(
  gen_random_uuid(),
  'SLIP_TRIP_FALL',
  'Kayma / Takılma / Düşme',
  '{
    "version": 1,
    "title": "Kayma / Takılma / Düşme",
    "fields": [
      { "key": "surface", "label": "Zemin Tipi", "type": "select", "required": true, "options": ["Islak","Kuru","Buzlu","Düzensiz"] },
      { "key": "ppeUsed", "label": "KKD Kullanıldı mı?", "type": "boolean", "required": true },
      { "key": "ppeType", "label": "KKD Türü", "type": "multiSelect", "options": ["Kaymaz Ayakkabı","Baret","Eldiven","Gözlük"], "visibleWhen": { "field": "ppeUsed", "equals": true } },
      { "key": "height", "label": "Yükseklik (m)", "type": "number", "visibleWhen": { "field": "isWorkingAtHeight", "equals": true } },
      { "key": "isWorkingAtHeight", "label": "Yüksekte çalışma var mı?", "type": "boolean" }
    ]
  }'::jsonb,
  TRUE
),
(
  gen_random_uuid(),
  'CUT_PUNCTURE',
  'Kesilme / Delinme',
  '{
    "version": 1,
    "title": "Kesilme / Delinme",
    "fields": [
      { "key": "toolType", "label": "Alet Türü", "type": "select", "required": true, "options": ["Bıçak","Makina","Cam","Diğer"] },
      { "key": "toolOther", "label": "Diğer (Açıklama)", "type": "text", "visibleWhen": { "field": "toolType", "equals": "Diğer" } },
      { "key": "guardPresent", "label": "Koruyucu mevcut mu?", "type": "boolean", "required": true }
    ]
  }'::jsonb,
  TRUE
)
ON CONFLICT (code) DO NOTHING;

-- Seed: notification rules (example)
INSERT INTO notification_rules (id, accident_class, potential_level, to_emails, cc_emails, enabled)
VALUES
(
  gen_random_uuid(),
  'MAJOR',
  'HIGH',
  'isg@company.com;isg2@company.com',
  'yonetim@company.com',
  TRUE
),
(
  gen_random_uuid(),
  'FATAL',
  'CRITICAL',
  'isg@company.com;acil@company.com',
  'yonetim@company.com',
  TRUE
)
ON CONFLICT (accident_class, potential_level) DO NOTHING;


