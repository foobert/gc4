CREATE TABLE IF NOT EXISTS geocache_ids (
    id INTEGER PRIMARY KEY,
    gccodes VARCHAR(8)[],
    ts TIMESTAMP WITH TIME ZONE
                                        );

CREATE TABLE IF NOT EXISTS raw_geocaches (
    id VARCHAR(8) PRIMARY KEY,
    raw JSONB,
    ts TIMESTAMP WITH TIME ZONE
)