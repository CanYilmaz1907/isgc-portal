CREATE TABLE IF NOT EXISTS accident_report_subscriptions (
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

CREATE INDEX IF NOT EXISTS idx_accident_report_subs_enabled ON accident_report_subscriptions(enabled);
CREATE INDEX IF NOT EXISTS idx_accident_report_subs_project_id ON accident_report_subscriptions(project_id);


