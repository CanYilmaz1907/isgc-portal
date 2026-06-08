-- Expand discipline_logs to match field specification
CREATE SEQUENCE IF NOT EXISTS discipline_logs_seq START 1;

ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS sequence_no INT;
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS full_name VARCHAR(200);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS employee_registration_no VARCHAR(80);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS company VARCHAR(200);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS job_title VARCHAR(150);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS work_area VARCHAR(200);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS category_level VARCHAR(30);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS violation_type VARCHAR(255);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS violation_description TEXT;
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS responsible_person VARCHAR(200);
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS repeat_count INT NOT NULL DEFAULT 0;
ALTER TABLE discipline_logs ADD COLUMN IF NOT EXISTS penalty_amount DECIMAL(12, 2);

-- Backfill sequence numbers for existing rows
DO $$
DECLARE r RECORD;
BEGIN
  FOR r IN SELECT id FROM discipline_logs WHERE sequence_no IS NULL ORDER BY occurred_at, created_at LOOP
    UPDATE discipline_logs SET sequence_no = nextval('discipline_logs_seq') WHERE id = r.id;
  END LOOP;
END $$;

-- Backfill violation_description from legacy description
UPDATE discipline_logs
SET violation_description = description
WHERE violation_description IS NULL AND description IS NOT NULL;

-- Migrate legacy status values
UPDATE discipline_logs SET status = 'UYARI' WHERE status = 'OPEN';
UPDATE discipline_logs SET status = 'IDARI_CEZA' WHERE status = 'CLOSED';

CREATE INDEX IF NOT EXISTS idx_discipline_employee_reg_no ON discipline_logs(employee_registration_no);
CREATE INDEX IF NOT EXISTS idx_discipline_category_level ON discipline_logs(category_level);
