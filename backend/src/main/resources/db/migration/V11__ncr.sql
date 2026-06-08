-- NCR (Non-Conformance Report / Sistemsel Uyumsuzluk Raporu)
CREATE TABLE IF NOT EXISTS ncr (
  id UUID PRIMARY KEY,
  ncr_number VARCHAR(80) NOT NULL UNIQUE,
  ncr_date DATE NOT NULL,
  project_id UUID REFERENCES projects(id),
  location VARCHAR(255),
  title VARCHAR(255),
  description TEXT,
  classification VARCHAR(80),  -- Sistemsel, Proses, Ürün, vb.
  root_cause TEXT,
  corrective_action TEXT,
  preventive_action TEXT,
  responsible_employee_id UUID REFERENCES employees(id),
  due_date DATE,
  closed_date DATE,
  status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  data JSONB NOT NULL DEFAULT '{}'::jsonb,
  assigned_by_user_id UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ncr_project_id ON ncr(project_id);
CREATE INDEX IF NOT EXISTS idx_ncr_ncr_date ON ncr(ncr_date);
CREATE INDEX IF NOT EXISTS idx_ncr_status ON ncr(status);
CREATE INDEX IF NOT EXISTS idx_ncr_responsible ON ncr(responsible_employee_id);
