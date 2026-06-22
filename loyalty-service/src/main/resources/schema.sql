CREATE TABLE IF NOT EXISTS customer_loyalty (
    id SERIAL PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    points INT NOT NULL,
    processed_supply_ids BIGINT[]
);
