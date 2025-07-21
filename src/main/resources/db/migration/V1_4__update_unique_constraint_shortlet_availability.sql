-- Drop the old unique constraint
ALTER TABLE shortlet_availability DROP CONSTRAINT IF EXISTS uksg2u1xmg3m5ly2v56sx6askeq;

-- Add new unique constraint including property_id
ALTER TABLE shortlet_availability ADD CONSTRAINT uksg2u1xmg3m5ly2v56sx6askeq UNIQUE (landlord_id, property_id, start_date, end_date);

