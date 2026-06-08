-- Kaza/Olay modülü genişletme: incident_no, classification, kişisel alanlar, neden tabloları

CREATE SEQUENCE IF NOT EXISTS accidents_incident_no_seq START WITH 1;

ALTER TABLE accidents
  ADD COLUMN IF NOT EXISTS incident_no INTEGER,
  ADD COLUMN IF NOT EXISTS classification VARCHAR(80),
  ADD COLUMN IF NOT EXISTS person_name VARCHAR(255),
  ADD COLUMN IF NOT EXISTS duration_on_project VARCHAR(120),
  ADD COLUMN IF NOT EXISTS duration_in_role VARCHAR(120),
  ADD COLUMN IF NOT EXISTS work_supervisor VARCHAR(255),
  ADD COLUMN IF NOT EXISTS emergency_notification_sent BOOLEAN,
  ADD COLUMN IF NOT EXISTS vehicle_plate VARCHAR(50),
  ADD COLUMN IF NOT EXISTS vehicle_type VARCHAR(80);

UPDATE accidents
SET incident_no = nextval('accidents_incident_no_seq')
WHERE incident_no IS NULL;

CREATE TABLE IF NOT EXISTS cause_categories (
  id UUID PRIMARY KEY,
  cause_type VARCHAR(20) NOT NULL,
  section VARCHAR(30) NOT NULL,
  group_code VARCHAR(10) NOT NULL,
  group_name VARCHAR(255) NOT NULL,
  item_code VARCHAR(10) NOT NULL,
  item_label VARCHAR(500) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_cause_categories_type_code UNIQUE (cause_type, item_code)
);

CREATE TABLE IF NOT EXISTS accident_direct_causes (
  id UUID PRIMARY KEY,
  accident_id UUID NOT NULL REFERENCES accidents(id) ON DELETE CASCADE,
  cause_code VARCHAR(10) NOT NULL,
  cause_label VARCHAR(500) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS accident_root_causes (
  id UUID PRIMARY KEY,
  accident_id UUID NOT NULL REFERENCES accidents(id) ON DELETE CASCADE,
  cause_code VARCHAR(10) NOT NULL,
  cause_label VARCHAR(500) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_accident_direct_causes_accident ON accident_direct_causes(accident_id);
CREATE INDEX IF NOT EXISTS idx_accident_root_causes_accident ON accident_root_causes(accident_id);
CREATE INDEX IF NOT EXISTS idx_accidents_classification ON accidents(classification);
CREATE INDEX IF NOT EXISTS idx_accidents_incident_no ON accidents(incident_no);

CREATE TABLE IF NOT EXISTS accident_lookup_options (
  id UUID PRIMARY KEY,
  option_type VARCHAR(40) NOT NULL,
  option_code VARCHAR(80) NOT NULL,
  option_label VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_accident_lookup_type_code UNIQUE (option_type, option_code)
);
