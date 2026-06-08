-- File metadata (generic)
CREATE TABLE IF NOT EXISTS file_objects (
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

CREATE INDEX IF NOT EXISTS idx_file_objects_module_entity ON file_objects(module, entity_id);

-- Accident type with dynamic schema
CREATE TABLE IF NOT EXISTS accident_types (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  form_schema JSONB NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Notification rules by accident class & potential
CREATE TABLE IF NOT EXISTS notification_rules (
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
CREATE TABLE IF NOT EXISTS accidents (
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

CREATE INDEX IF NOT EXISTS idx_accidents_project_id ON accidents(project_id);
CREATE INDEX IF NOT EXISTS idx_accidents_created_at ON accidents(created_at);

-- Accident persons (injured/key)
CREATE TABLE IF NOT EXISTS accident_people (
  id UUID PRIMARY KEY,
  accident_id UUID NOT NULL REFERENCES accidents(id) ON DELETE CASCADE,
  employee_id UUID NOT NULL REFERENCES employees(id),
  role VARCHAR(20) NOT NULL, -- INJURED / KEY_PERSON
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(accident_id, employee_id, role)
);

CREATE INDEX IF NOT EXISTS idx_accident_people_accident_id ON accident_people(accident_id);


