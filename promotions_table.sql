-- Create promotions table
CREATE TABLE IF NOT EXISTS promotions (
    promotion_id BIGSERIAL PRIMARY KEY,
    promotion_name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add check constraint for status
ALTER TABLE promotions 
ADD CONSTRAINT chk_promotion_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'));

-- Create indexes for better performance
CREATE INDEX idx_promotions_status ON promotions(status);
CREATE INDEX idx_promotions_date_range ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_status_date ON promotions(status, start_date, end_date);

-- Insert some sample data (optional)
INSERT INTO promotions (promotion_name, description, image_url, start_date, end_date, status) VALUES
('Summer Special', 'Get 20% off on all haircuts this summer!', 'https://example.com/summer-promo.jpg', '2025-06-01', '2025-08-31', 'ACTIVE'),
('New Customer Offer', 'First-time customers get 30% off', 'https://example.com/new-customer.jpg', '2025-01-01', '2025-12-31', 'ACTIVE'),
('Weekend Discount', 'Special weekend pricing for all services', 'https://example.com/weekend-deal.jpg', '2025-09-01', '2025-09-30', 'ACTIVE');
