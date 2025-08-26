-- Add timestamp columns to shortlet_booking table
ALTER TABLE shortlet_booking
ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Create index on created_at for cleanup operations
CREATE INDEX idx_shortlet_booking_created_at ON shortlet_booking(created_at);

-- Create index on updated_at for performance
CREATE INDEX idx_shortlet_booking_updated_at ON shortlet_booking(updated_at);

-- Create trigger to auto-update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_shortlet_booking_updated_at
    BEFORE UPDATE ON shortlet_booking
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
