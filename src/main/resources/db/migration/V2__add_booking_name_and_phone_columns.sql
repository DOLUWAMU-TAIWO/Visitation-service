-- Add missing columns to shortlet_booking for name and phone support
ALTER TABLE shortlet_booking
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN phone_number VARCHAR(255);

