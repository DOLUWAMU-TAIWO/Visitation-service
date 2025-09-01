-- V9__add_booking_validation_constraints.sql
-- Add comprehensive database constraints and indexes for booking validation
-- Rewritten to avoid DO blocks and handle existing constraints gracefully

-- 1. Add unique constraint to prevent duplicate pending bookings
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_pending_booking
    ON shortlet_booking (tenant_id, landlord_id, property_id, start_date, end_date)
    WHERE status = 'PENDING';

-- 2. Add constraint to ensure tenant != landlord (safe for existing data)
-- Check if constraint exists before adding
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_tenant_landlord_different'
        AND table_name = 'shortlet_booking'
    ) THEN
        ALTER TABLE shortlet_booking
        ADD CONSTRAINT chk_tenant_landlord_different
        CHECK (tenant_id != landlord_id);
    END IF;
END $$;

-- 3. Fix any invalid date range data first (only if constraint doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_valid_date_range'
        AND table_name = 'shortlet_booking'
    ) THEN
        DELETE FROM shortlet_booking
        WHERE start_date >= end_date
          AND start_date IS NOT NULL
          AND end_date IS NOT NULL;

        -- Add constraint to ensure start_date < end_date (handle NULLs)
        ALTER TABLE shortlet_booking
        ADD CONSTRAINT chk_valid_date_range
        CHECK (start_date IS NULL OR end_date IS NULL OR start_date < end_date);
    END IF;
END $$;

-- 4. Add constraint to prevent NEW bookings in the past (safe for existing data)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_future_booking'
        AND table_name = 'shortlet_booking'
    ) THEN
        ALTER TABLE shortlet_booking
        ADD CONSTRAINT chk_future_booking
        CHECK (start_date >= CURRENT_DATE OR created_at < NOW()::date);
    END IF;
END $$;

-- 5. Add flexible email validation (more permissive)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_valid_email'
        AND table_name = 'shortlet_booking'
    ) THEN
        ALTER TABLE shortlet_booking
        ADD CONSTRAINT chk_valid_email
        CHECK (tenant_email IS NULL OR tenant_email ~ '^.+@.+\..+$');
    END IF;
END $$;

-- 6. Add amount validation (handle NULLs)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_positive_amount'
        AND table_name = 'shortlet_booking'
    ) THEN
        ALTER TABLE shortlet_booking
        ADD CONSTRAINT chk_positive_amount
        CHECK (total_amount IS NULL OR total_amount > 0);
    END IF;
END $$;

-- 7. Performance indexes for booking queries
CREATE INDEX IF NOT EXISTS idx_booking_landlord_property_dates
ON shortlet_booking (landlord_id, property_id, start_date, end_date);

CREATE INDEX IF NOT EXISTS idx_booking_tenant_status_dates
ON shortlet_booking (tenant_id, status, start_date DESC);

CREATE INDEX IF NOT EXISTS idx_booking_property_status_dates
ON shortlet_booking (property_id, status, start_date, end_date);

-- 8. Performance indexes for availability queries
CREATE INDEX IF NOT EXISTS idx_availability_landlord_property_dates
ON shortlet_availability (landlord_id, property_id, start_date, end_date);

-- 9. Add constraint to availability (handle NULLs)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'chk_availability_valid_range'
        AND table_name = 'shortlet_availability'
    ) THEN
        ALTER TABLE shortlet_availability
        ADD CONSTRAINT chk_availability_valid_range
        CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date);
    END IF;
END $$;

-- 10. Add partial index for active bookings performance
CREATE INDEX IF NOT EXISTS idx_active_bookings
ON shortlet_booking (property_id, start_date, end_date)
WHERE status IN ('PENDING', 'ACCEPTED');

-- 11. Create audit table for status changes
CREATE TABLE IF NOT EXISTS booking_status_audit (
    id SERIAL PRIMARY KEY,
    booking_id UUID NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_at TIMESTAMP DEFAULT NOW(),
    changed_by VARCHAR(100)
);

-- 12. Create status transition validation function
CREATE OR REPLACE FUNCTION validate_booking_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- Allow any status for new records
    IF TG_OP = 'INSERT' THEN
        RETURN NEW;
    END IF;
    
    -- Only validate critical transitions (more flexible)
    -- Allow admin overrides and emergency changes
    IF OLD.status = 'COMPLETED' AND NEW.status = 'PENDING' THEN
        RAISE EXCEPTION 'Cannot revert completed booking to pending';
    ELSIF OLD.status = 'PENDING' AND NEW.status = 'COMPLETED' THEN
        RAISE EXCEPTION 'Cannot skip from pending directly to completed';
    END IF;
    
    -- Log all status changes for audit (avoid duplicates)
    IF NOT EXISTS (
        SELECT 1 FROM booking_status_audit 
        WHERE booking_id = NEW.id 
        AND old_status = OLD.status 
        AND new_status = NEW.status 
        AND changed_at > NOW() - INTERVAL '1 minute'
    ) THEN
        INSERT INTO booking_status_audit (booking_id, old_status, new_status, changed_at, changed_by)
        VALUES (NEW.id, OLD.status, NEW.status, NOW(), current_user);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 13. Create the status transition trigger
DROP TRIGGER IF EXISTS trg_validate_booking_status ON shortlet_booking;
CREATE TRIGGER trg_validate_booking_status
    BEFORE UPDATE ON shortlet_booking
    FOR EACH ROW
    EXECUTE FUNCTION validate_booking_status_transition();

-- 14. Add comprehensive comments for documentation
COMMENT ON TABLE shortlet_booking IS 'Main booking table with enhanced validation constraints';
COMMENT ON TABLE booking_status_audit IS 'Audit trail for booking status changes';

-- 15. Create rollback function for emergency use
CREATE OR REPLACE FUNCTION rollback_v9_constraints()
RETURNS void AS $$
BEGIN
    -- Drop all constraints added in this migration
    ALTER TABLE shortlet_booking DROP CONSTRAINT IF EXISTS chk_tenant_landlord_different;
    ALTER TABLE shortlet_booking DROP CONSTRAINT IF EXISTS chk_valid_date_range;
    ALTER TABLE shortlet_booking DROP CONSTRAINT IF EXISTS chk_future_booking;
    ALTER TABLE shortlet_booking DROP CONSTRAINT IF EXISTS chk_valid_email;
    ALTER TABLE shortlet_booking DROP CONSTRAINT IF EXISTS chk_positive_amount;
    ALTER TABLE shortlet_availability DROP CONSTRAINT IF EXISTS chk_availability_valid_range;
    
    -- Drop indexes
    DROP INDEX IF EXISTS idx_unique_pending_booking;
    DROP INDEX IF EXISTS idx_booking_landlord_property_dates;
    DROP INDEX IF EXISTS idx_booking_tenant_status_dates;
    DROP INDEX IF EXISTS idx_booking_property_status_dates;
    DROP INDEX IF EXISTS idx_availability_landlord_property_dates;
    DROP INDEX IF EXISTS idx_active_bookings;
    
    -- Drop trigger and function
    DROP TRIGGER IF EXISTS trg_validate_booking_status ON shortlet_booking;
    DROP FUNCTION IF EXISTS validate_booking_status_transition();
    
    RAISE NOTICE 'V9 migration rolled back successfully';
END;
$$ LANGUAGE plpgsql;
