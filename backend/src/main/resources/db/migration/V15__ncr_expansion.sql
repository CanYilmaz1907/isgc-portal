-- Expand NCR to match specification
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS responsible_organization VARCHAR(255);
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS evidence_references TEXT;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS proposed_corrective_action TEXT;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS executed_corrective_action TEXT;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS target_completion_date DATE;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS completion_date DATE;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS root_cause_categories JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS initiated_by VARCHAR(200);
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS approved_by VARCHAR(200);
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS verified_by VARCHAR(200);
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS verification_status VARCHAR(30);
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS iso_standards JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS followup_required BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE ncr ADD COLUMN IF NOT EXISTS notes TEXT;

-- Backfill from legacy fields
UPDATE ncr SET proposed_corrective_action = corrective_action WHERE proposed_corrective_action IS NULL AND corrective_action IS NOT NULL;
UPDATE ncr SET target_completion_date = due_date WHERE target_completion_date IS NULL AND due_date IS NOT NULL;
UPDATE ncr SET completion_date = closed_date WHERE completion_date IS NULL AND closed_date IS NOT NULL;

-- Migrate status workflow
UPDATE ncr SET status = 'CORRECTIVE_ACTION_PENDING' WHERE status = 'IN_PROGRESS';
