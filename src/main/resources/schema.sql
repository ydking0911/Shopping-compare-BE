-- Trend Aggregation Tables
CREATE TABLE IF NOT EXISTS trend_agg_daily (
    keyword VARCHAR(100) NOT NULL,
    agg_date DATE NOT NULL,
    avg_ratio DECIMAL(10,4) NOT NULL,
    max_ratio DECIMAL(10,4) NOT NULL,
    min_ratio DECIMAL(10,4) NOT NULL,
    total_click BIGINT NOT NULL DEFAULT 0,
    direction VARCHAR(20) NOT NULL,
    strength DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (keyword, agg_date),
    INDEX idx_agg_date (agg_date),
    INDEX idx_keyword (keyword)
);

CREATE TABLE IF NOT EXISTS trend_agg_weekly (
    keyword VARCHAR(100) NOT NULL,
    year_week VARCHAR(10) NOT NULL, -- YYYY-WW format
    avg_ratio DECIMAL(10,4) NOT NULL,
    max_ratio DECIMAL(10,4) NOT NULL,
    min_ratio DECIMAL(10,4) NOT NULL,
    total_click BIGINT NOT NULL DEFAULT 0,
    direction VARCHAR(20) NOT NULL,
    strength DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (keyword, year_week),
    INDEX idx_year_week (year_week),
    INDEX idx_keyword (keyword)
);

CREATE TABLE IF NOT EXISTS trend_agg_monthly (
    keyword VARCHAR(100) NOT NULL,
    year_month VARCHAR(7) NOT NULL, -- YYYY-MM format
    avg_ratio DECIMAL(10,4) NOT NULL,
    max_ratio DECIMAL(10,4) NOT NULL,
    total_click BIGINT NOT NULL DEFAULT 0,
    direction VARCHAR(20) NOT NULL,
    strength DECIMAL(10,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (keyword, year_month),
    INDEX idx_year_month (year_month),
    INDEX idx_keyword (keyword)
);
