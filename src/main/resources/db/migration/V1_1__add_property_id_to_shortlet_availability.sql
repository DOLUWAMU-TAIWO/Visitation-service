-- Add property_id as nullable
ALTER TABLE shortlet_availability ADD COLUMN property_id UUID;

-- Backfill property_id for existing rows with a dummy value (update this in production!)
UPDATE shortlet_availability SET property_id = '00000000-0000-0000-0000-000000000000' WHERE property_id IS NULL;

-- Set property_id as NOT NULL
ALTER TABLE shortlet_availability ALTER COLUMN property_id SET NOT NULL;

