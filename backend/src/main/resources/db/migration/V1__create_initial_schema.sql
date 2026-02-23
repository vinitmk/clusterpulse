
CREATE TABLE nodes (
    id UUID PRIMARY KEY,
    node_id VARCHAR(255) UNIQUE NOT NULL,
    node_name VARCHAR(255),
    region VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMPTZ
);

CREATE TABLE node_metrics (
    id UUID PRIMARY KEY,
    node_id VARCHAR(255) NOT NULL,
    cpu_percent DOUBLE PRECISION NOT NULL,
    memory_percent DOUBLE PRECISION NOT NULL,
    network_latency_ms DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_node_metrics_node_id_recorded_at_desc
    ON node_metrics (node_id, recorded_at DESC);

-- Convert node_metrics to a TimescaleDB hypertable on recorded_at
SELECT create_hypertable('node_metrics', 'recorded_at', if_not_exists => TRUE);


