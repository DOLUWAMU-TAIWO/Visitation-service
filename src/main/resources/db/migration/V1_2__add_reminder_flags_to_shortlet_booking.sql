-- Add reminder_24h_sent and reminder_1h_sent columns to shortlet_booking if they do not exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='shortlet_booking' AND column_name='reminder_24h_sent'
    ) THEN
        ALTER TABLE shortlet_booking ADD COLUMN reminder_24h_sent BOOLEAN DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='shortlet_booking' AND column_name='reminder_1h_sent'
    ) THEN
        ALTER TABLE shortlet_booking ADD COLUMN reminder_1h_sent BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

