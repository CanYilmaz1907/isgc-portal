-- Extend employees with profession (needed by discipline log)
ALTER TABLE employees
  ADD COLUMN IF NOT EXISTS profession VARCHAR(150);

-- Discipline logs
CREATE TABLE IF NOT EXISTS discipline_logs (
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

CREATE INDEX IF NOT EXISTS idx_discipline_project_id ON discipline_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_discipline_occurred_at ON discipline_logs(occurred_at);
CREATE INDEX IF NOT EXISTS idx_discipline_employee_id ON discipline_logs(violating_employee_id);


