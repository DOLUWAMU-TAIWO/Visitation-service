-- Add payment service integration fields to shortlet_booking table
ALTER TABLE shortlet_booking
ADD COLUMN tenant_email VARCHAR(255),
ADD COLUMN total_amount DECIMAL(10,2),
ADD COLUMN currency VARCHAR(3) DEFAULT 'NGN';

-- Create index on tenant_email for faster lookups
CREATE INDEX idx_shortlet_booking_tenant_email ON shortlet_booking(tenant_email);

-- Create index on currency for reporting queries
CREATE INDEX idx_shortlet_booking_currency ON shortlet_booking(currency);
