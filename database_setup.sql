-- =====================================================
-- ISG-C Portal - Complete Database Setup Script
-- =====================================================
-- Bu dosya tüm Flyway migration'larını (V1-V8) tek bir SQL dosyasında toplar.
-- Kendi veritabanınızda çalıştırmak için bu dosyayı kullanabilirsiniz.
-- 
-- Kullanım:
--   1. PostgreSQL veritabanınızı oluşturun: CREATE DATABASE isgc_portal;
--   2. Bu dosyayı psql veya pgAdmin ile çalıştırın
-- =====================================================

-- =====================================================
-- V1: Core Schema (Users, Projects, Employees, Training, Audit Logs)
-- =====================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

CREATE TABLE projects (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE employees (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  employee_no VARCHAR(50),
  first_name VARCHAR(120) NOT NULL,
  last_name VARCHAR(120) NOT NULL,
  job_title VARCHAR(120),
  primary_manager_employee_id UUID REFERENCES employees(id),
  user_id UUID UNIQUE REFERENCES users(id),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_employees_project_id ON employees(project_id);

CREATE TABLE training_records (
  id UUID PRIMARY KEY,
  employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  training_name VARCHAR(255) NOT NULL,
  provider VARCHAR(255),
  completed_on DATE,
  valid_until DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_training_records_employee_id ON training_records(employee_id);

CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  actor_user_id UUID REFERENCES users(id),
  actor_username VARCHAR(80),
  action VARCHAR(50) NOT NULL,
  entity_type VARCHAR(120),
  entity_id UUID,
  details JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- =====================================================
-- V2: Files and Accidents Module
-- =====================================================

-- File metadata (generic)
CREATE TABLE file_objects (
  id UUID PRIMARY KEY,
  module VARCHAR(50) NOT NULL,
  entity_id UUID NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  content_type VARCHAR(150),
  size_bytes BIGINT NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  sha256 VARCHAR(64),
  uploaded_by_user_id UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_file_objects_module_entity ON file_objects(module, entity_id);

-- Accident type with dynamic schema
CREATE TABLE accident_types (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  form_schema JSONB NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Notification rules by accident class & potential
CREATE TABLE notification_rules (
  id UUID PRIMARY KEY,
  accident_class VARCHAR(50) NOT NULL,
  potential_level VARCHAR(50) NOT NULL,
  to_emails TEXT NOT NULL,
  cc_emails TEXT,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(accident_class, potential_level)
);

-- Accidents
CREATE TABLE accidents (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  accident_type_id UUID NOT NULL REFERENCES accident_types(id),
  reported_by_user_id UUID REFERENCES users(id),
  occurred_at TIMESTAMPTZ,
  location VARCHAR(255),
  accident_class VARCHAR(50) NOT NULL,
  potential_level VARCHAR(50) NOT NULL,
  description TEXT,
  form_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  root_cause_data JSONB NOT NULL DEFAULT '{}'::jsonb,
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accidents_project_id ON accidents(project_id);
CREATE INDEX idx_accidents_created_at ON accidents(created_at);

-- Accident persons (injured/key)
CREATE TABLE accident_people (
  id UUID PRIMARY KEY,
  accident_id UUID NOT NULL REFERENCES accidents(id) ON DELETE CASCADE,
  employee_id UUID NOT NULL REFERENCES employees(id),
  role VARCHAR(20) NOT NULL, -- INJURED / KEY_PERSON
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(accident_id, employee_id, role)
);

CREATE INDEX idx_accident_people_accident_id ON accident_people(accident_id);

-- =====================================================
-- V3: Seed Accident Types and Notification Rules
-- =====================================================

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
);

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

-- =====================================================
-- V4: Nonconformity Tracking Module
-- =====================================================

-- Hazard classes (from Excel; can be expanded)
CREATE TABLE hazard_classes (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Nonconformity templates: preserve current table structure via JSON schema
CREATE TABLE nonconformity_templates (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  table_schema JSONB NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Nonconformities
CREATE TABLE nonconformities (
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

CREATE INDEX idx_nonconf_project_id ON nonconformities(project_id);
CREATE INDEX idx_nonconf_status ON nonconformities(status);
CREATE INDEX idx_nonconf_hazard_class ON nonconformities(hazard_class_id);
CREATE INDEX idx_nonconf_responsible ON nonconformities(responsible_employee_id);

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

-- =====================================================
-- V5: Discipline Logs Module
-- =====================================================

-- Extend employees with profession (needed by discipline log)
ALTER TABLE employees
  ADD COLUMN IF NOT EXISTS profession VARCHAR(150);

-- Discipline logs
CREATE TABLE discipline_logs (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  occurred_at TIMESTAMPTZ NOT NULL,
  category VARCHAR(120),
  description TEXT NOT NULL,
  severity INT NOT NULL DEFAULT 1,
  profession VARCHAR(150),
  violating_employee_id UUID REFERENCES employees(id),
  violating_manager_employee_id UUID REFERENCES employees(id),
  created_by_user_id UUID REFERENCES users(id),
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN / CLOSED
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_discipline_project_id ON discipline_logs(project_id);
CREATE INDEX idx_discipline_occurred_at ON discipline_logs(occurred_at);
CREATE INDEX idx_discipline_employee_id ON discipline_logs(violating_employee_id);

-- =====================================================
-- V6: Audits and Checklists Module
-- =====================================================

-- Checklists
CREATE TABLE checklists (
  id UUID PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  scope VARCHAR(30) NOT NULL DEFAULT 'GENERAL', -- future-proof
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE checklist_items (
  id UUID PRIMARY KEY,
  checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
  item_no INT NOT NULL,
  question TEXT NOT NULL,
  weight NUMERIC(8,2) NOT NULL DEFAULT 1.0,
  max_score NUMERIC(8,2) NOT NULL DEFAULT 1.0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(checklist_id, item_no)
);

CREATE INDEX idx_checklist_items_checklist_id ON checklist_items(checklist_id);

-- Audits (internal/external/checklist-based)
CREATE TABLE audits (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  audit_type VARCHAR(30) NOT NULL, -- INTERNAL / EXTERNAL
  checklist_id UUID REFERENCES checklists(id),
  title VARCHAR(300) NOT NULL,
  summary TEXT,
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT / IN_PROGRESS / COMPLETED
  started_at TIMESTAMPTZ,
  finished_at TIMESTAMPTZ,
  calculated_score NUMERIC(8,2),
  report_html TEXT,
  created_by_user_id UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audits_project_id ON audits(project_id);
CREATE INDEX idx_audits_status ON audits(status);
CREATE INDEX idx_audits_created_at ON audits(created_at);

-- Audit item results
CREATE TABLE audit_item_results (
  id UUID PRIMARY KEY,
  audit_id UUID NOT NULL REFERENCES audits(id) ON DELETE CASCADE,
  checklist_item_id UUID NOT NULL REFERENCES checklist_items(id) ON DELETE RESTRICT,
  score NUMERIC(8,2) NOT NULL,
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(audit_id, checklist_item_id)
);

CREATE INDEX idx_audit_item_results_audit_id ON audit_item_results(audit_id);

-- Participants
CREATE TABLE audit_participants (
  id UUID PRIMARY KEY,
  audit_id UUID NOT NULL REFERENCES audits(id) ON DELETE CASCADE,
  employee_id UUID REFERENCES employees(id),
  role VARCHAR(120),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_participants_audit_id ON audit_participants(audit_id);

-- Optional: project notification emails for audit completion
ALTER TABLE projects ADD COLUMN IF NOT EXISTS audit_notification_emails TEXT;

-- =====================================================
-- V7: Document Management Module
-- =====================================================

CREATE TABLE documents (
  id UUID PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(300) NOT NULL,
  description TEXT,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE document_versions (
  id UUID PRIMARY KEY,
  document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
  version INT NOT NULL,
  note TEXT,
  created_by_user_id UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(document_id, version)
);

CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);

-- =====================================================
-- V8: Accident Reporting Module
-- =====================================================

CREATE TABLE accident_report_subscriptions (
  id UUID PRIMARY KEY,
  project_id UUID REFERENCES projects(id),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  frequency VARCHAR(20) NOT NULL, -- DAILY / WEEKLY / MONTHLY
  hour_of_day INT NOT NULL DEFAULT 9,
  minute_of_hour INT NOT NULL DEFAULT 0,
  to_emails TEXT NOT NULL,
  cc_emails TEXT,
  filters JSONB NOT NULL DEFAULT '{}'::jsonb, -- { "accidentClass": "...", "potentialLevel": "...", "periodDays": 30 }
  last_sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accident_report_subs_enabled ON accident_report_subscriptions(enabled);
CREATE INDEX idx_accident_report_subs_project_id ON accident_report_subscriptions(project_id);

-- =====================================================
-- Setup Complete
-- =====================================================
-- Tüm tablolar ve seed veriler oluşturuldu.
-- 
-- Not: İlk kullanıcılar (Irina ve Samet) backend uygulaması başlatıldığında
-- DataSeeder tarafından otomatik olarak oluşturulacaktır.
-- =====================================================


