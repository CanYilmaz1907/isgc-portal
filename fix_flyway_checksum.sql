-- Fix Flyway checksum for V10 migration
-- This updates the checksum in flyway_schema_history to match the current V10 file

UPDATE flyway_schema_history 
SET checksum = -833909329 
WHERE version = '10' AND checksum = 2136811394;

