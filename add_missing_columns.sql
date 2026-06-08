-- Add missing columns from V10 migration
-- Run this script manually if V10 migration wasn't applied

ALTER TABLE accidents 
ADD COLUMN IF NOT EXISTS group_company_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS responsible_person VARCHAR(255),
ADD COLUMN IF NOT EXISTS estimated_cost VARCHAR(100),
ADD COLUMN IF NOT EXISTS work_related BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS work_during_accident VARCHAR(500),
ADD COLUMN IF NOT EXISTS injured_person_age INTEGER,
ADD COLUMN IF NOT EXISTS injured_person_profession VARCHAR(255),
ADD COLUMN IF NOT EXISTS injured_person_gender VARCHAR(20),
ADD COLUMN IF NOT EXISTS injured_person_nationality VARCHAR(100),
ADD COLUMN IF NOT EXISTS injured_person_company VARCHAR(255),
ADD COLUMN IF NOT EXISTS actions_taken JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS prepared_by_user_id UUID REFERENCES users(id),
ADD COLUMN IF NOT EXISTS prepared_at TIMESTAMPTZ;

-- Create index for prepared_by lookup
CREATE INDEX IF NOT EXISTS idx_accidents_prepared_by ON accidents(prepared_by_user_id);

