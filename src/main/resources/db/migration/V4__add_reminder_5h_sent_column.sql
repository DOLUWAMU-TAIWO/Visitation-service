-- Add reminder_5h_sent column to shortlet_booking table
ALTER TABLE shortlet_booking
ADD COLUMN reminder_5h_sent BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for performance on reminder queries
CREATE INDEX idx_shortlet_booking_reminder_5h_sent ON shortlet_booking(reminder_5h_sent);

-- Update comment to reflect the new reminder stages
COMMENT ON COLUMN shortlet_booking.reminder_24h_sent IS '24-hour reminder sent flag';
COMMENT ON COLUMN shortlet_booking.reminder_5h_sent IS '5-hour reminder sent flag';
COMMENT ON COLUMN shortlet_booking.reminder_1h_sent IS '1-hour reminder sent flag';
