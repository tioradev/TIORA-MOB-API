-- Add table for barber device tokens
CREATE TABLE IF NOT EXISTS barber_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    barber_id BIGINT NOT NULL,
    device_token VARCHAR(500) NOT NULL,
    device_type VARCHAR(20),
    app_version VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    
    CONSTRAINT unique_active_token UNIQUE (device_token, is_active)
);

-- Add index for faster barber lookup
CREATE INDEX IF NOT EXISTS idx_barber_device_tokens_barber_id ON barber_device_tokens(barber_id);
CREATE INDEX IF NOT EXISTS idx_barber_device_tokens_active ON barber_device_tokens(is_active);
CREATE INDEX IF NOT EXISTS idx_barber_device_tokens_last_used ON barber_device_tokens(last_used_at);

-- Add foreign key constraint to employees table (assuming barber_id references employee_id)
-- ALTER TABLE barber_device_tokens ADD CONSTRAINT fk_barber_device_tokens_employee 
-- FOREIGN KEY (barber_id) REFERENCES employees(employee_id) ON DELETE CASCADE;
