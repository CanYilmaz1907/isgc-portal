-- Add only the missing actions_taken column
ALTER TABLE accidents 
ADD COLUMN IF NOT EXISTS actions_taken JSONB DEFAULT '[]'::jsonb;

