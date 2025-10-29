-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Outbox events table
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes for outbox events
CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);

-- Changesets table
CREATE TABLE changesets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    change_type VARCHAR(50) NOT NULL,
    geometry GEOMETRY(Polygon, 4326),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    metadata TEXT
);

-- Indexes for changesets
CREATE INDEX idx_changeset_created_at ON changesets(created_at);
CREATE INDEX idx_changeset_type ON changesets(change_type);

-- Spatial index for geometry column
CREATE INDEX idx_changeset_geometry ON changesets USING GIST(geometry);
