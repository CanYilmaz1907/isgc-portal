-- Checklists
CREATE TABLE IF NOT EXISTS checklists (
  id UUID PRIMARY KEY,
  code VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL,
  scope VARCHAR(30) NOT NULL DEFAULT 'GENERAL', -- future-proof
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS checklist_items (
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

CREATE INDEX IF NOT EXISTS idx_checklist_items_checklist_id ON checklist_items(checklist_id);

-- Audits (internal/external/checklist-based)
CREATE TABLE IF NOT EXISTS audits (
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

CREATE INDEX IF NOT EXISTS idx_audits_project_id ON audits(project_id);
CREATE INDEX IF NOT EXISTS idx_audits_status ON audits(status);
CREATE INDEX IF NOT EXISTS idx_audits_created_at ON audits(created_at);

-- Audit item results
CREATE TABLE IF NOT EXISTS audit_item_results (
  id UUID PRIMARY KEY,
  audit_id UUID NOT NULL REFERENCES audits(id) ON DELETE CASCADE,
  checklist_item_id UUID NOT NULL REFERENCES checklist_items(id) ON DELETE RESTRICT,
  score NUMERIC(8,2) NOT NULL,
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(audit_id, checklist_item_id)
);

CREATE INDEX IF NOT EXISTS idx_audit_item_results_audit_id ON audit_item_results(audit_id);

-- Participants
CREATE TABLE IF NOT EXISTS audit_participants (
  id UUID PRIMARY KEY,
  audit_id UUID NOT NULL REFERENCES audits(id) ON DELETE CASCADE,
  employee_id UUID REFERENCES employees(id),
  role VARCHAR(120),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_participants_audit_id ON audit_participants(audit_id);

-- Optional: project notification emails for audit completion
ALTER TABLE projects ADD COLUMN IF NOT EXISTS audit_notification_emails TEXT;


