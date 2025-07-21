-- Add payment fields and contact info to shortlet_booking only if they do not exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='shortlet_booking' AND column_name='payment_status'
    ) THEN
        ALTER TABLE shortlet_booking ADD COLUMN payment_status VARCHAR(32);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='shortlet_booking' AND column_name='payment_reference'
    ) THEN
        ALTER TABLE shortlet_booking ADD COLUMN payment_reference VARCHAR(128);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns WHERE table_name='shortlet_booking' AND column_name='payment_amount'
    ) THEN
        ALTER TABLE shortlet_booking ADD COLUMN payment_amount DECIMAL(10,2);
    END IF;
END$$;

-- The contact info fields (first_name, last_name, phone_number) are already present, so no need to add them again.
