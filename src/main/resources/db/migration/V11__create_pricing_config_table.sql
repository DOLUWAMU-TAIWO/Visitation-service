-- Migration to create pricing_config table and initialize default configuration
CREATE TABLE pricing_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_name VARCHAR(255) UNIQUE NOT NULL,
    service_fee_percentage DECIMAL(5,4) NOT NULL,
    tax_percentage DECIMAL(5,4) NOT NULL,
    cleaning_fee_fixed DECIMAL(15,2),
    cleaning_fee_per_guest DECIMAL(15,2),
    max_guests_base_rate INTEGER NOT NULL,
    extra_guest_fee DECIMAL(15,2),
    weekend_multiplier DECIMAL(5,4),
    peak_season_multiplier DECIMAL(5,4),
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    quote_validity_hours INTEGER NOT NULL DEFAULT 24,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Insert default pricing configuration for Nigerian market
INSERT INTO pricing_config (
    config_name,
    service_fee_percentage,
    tax_percentage,
    cleaning_fee_fixed,
    cleaning_fee_per_guest,
    max_guests_base_rate,
    extra_guest_fee,
    weekend_multiplier,
    peak_season_multiplier,
    currency,
    quote_validity_hours
) VALUES (
    'default',
    0.0500,  -- 5% service fee
    0.0750,  -- 7.5% VAT (Nigerian standard rate)
    50000.00, -- ₦50,000 base cleaning fee
    10000.00, -- ₦10,000 additional cleaning per extra guest
    2,        -- Base rate includes up to 2 guests
    25000.00, -- ₦25,000 per extra guest per night
    1.2000,   -- 20% weekend markup
    1.5000,   -- 50% peak season markup
    'NGN',
    24        -- Quote valid for 24 hours
);
