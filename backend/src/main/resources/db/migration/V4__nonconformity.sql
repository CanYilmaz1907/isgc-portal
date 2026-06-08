-- Hazard classes (from Excel; can be expanded)
CREATE TABLE IF NOT EXISTS hazard_classes (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Nonconformity templates: preserve current table structure via JSON schema
CREATE TABLE IF NOT EXISTS nonconformity_templates (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  table_schema JSONB NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Nonconformities
CREATE TABLE IF NOT EXISTS nonconformities (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  template_id UUID NOT NULL REFERENCES nonconformity_templates(id),
  hazard_class_id UUID REFERENCES hazard_classes(id),
  responsible_employee_id UUID REFERENCES employees(id),
  assigned_by_user_id UUID REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  due_date DATE,
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN / IN_PROGRESS / CLOSED
  severity VARCHAR(30), -- optional
  data JSONB NOT NULL DEFAULT '{}'::jsonb, -- dynamic columns
  last_reminded_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_nonconf_project_id ON nonconformities(project_id);
CREATE INDEX IF NOT EXISTS idx_nonconf_status ON nonconformities(status);
CREATE INDEX IF NOT EXISTS idx_nonconf_hazard_class ON nonconformities(hazard_class_id);
CREATE INDEX IF NOT EXISTS idx_nonconf_responsible ON nonconformities(responsible_employee_id);

-- Seed minimal hazard classes
INSERT INTO hazard_classes (id, code, name, enabled)
VALUES
  (gen_random_uuid(), 'COK_TEHLIKELI', 'Çok Tehlikeli', TRUE),
  (gen_random_uuid(), 'TEHLIKELI', 'Tehlikeli', TRUE),
  (gen_random_uuid(), 'AZ_TEHLIKELI', 'Az Tehlikeli', TRUE)
ON CONFLICT (code) DO NOTHING;

-- Seed a default template (can be replaced with your exact Excel columns)
INSERT INTO nonconformity_templates (id, code, name, table_schema, enabled)
VALUES
(
  gen_random_uuid(),
  'DEFAULT',
  'Varsayılan Uygunsuzluk Tablosu',
  '{
    "version": 1,
    "columns": [
      { "key": "location", "label": "Lokasyon", "type": "text", "required": false },
      { "key": "category", "label": "Kategori", "type": "select", "options": ["İSG","Kalite","Çevre","Diğer"] },
      { "key": "risk", "label": "Risk / Tehlike", "type": "text" },
      { "key": "action", "label": "Aksiyon", "type": "text" },
      { "key": "evidence", "label": "Kanıt", "type": "text" }
    ]
  }'::jsonb,
  TRUE
)
ON CONFLICT (code) DO NOTHING;


