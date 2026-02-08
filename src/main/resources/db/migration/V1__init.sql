CREATE TABLE monsters (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    element VARCHAR(50) NOT NULL,
    stats_hp BIGINT NOT NULL,
    stats_atk BIGINT NOT NULL,
    stats_def BIGINT NOT NULL,
    stats_vit BIGINT NOT NULL,
    rank VARCHAR(50) NOT NULL,
    visual_description TEXT,
    card_description VARCHAR(255),
    image_url TEXT
);

CREATE TABLE skills (
    id VARCHAR(36) PRIMARY KEY,
    monster_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    damage BIGINT NOT NULL,
    ratio_stat VARCHAR(50),
    ratio_percent DOUBLE PRECISION,
    cooldown BIGINT NOT NULL,
    lvl_max BIGINT NOT NULL,
    rank VARCHAR(50) NOT NULL,
    CONSTRAINT fk_skills_monsters
        FOREIGN KEY (monster_id)
        REFERENCES monsters (id)
        ON DELETE CASCADE
);

CREATE TABLE invocation_buffer (
    id VARCHAR(36) PRIMARY KEY,
    player_id VARCHAR(255),
    monster_snapshot JSONB,
    monster_request JSONB,
    monster_response JSONB,
    player_request JSONB,
    player_response JSONB,
    status VARCHAR(50),
    failure_reason TEXT,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    last_attempt_at TIMESTAMP
);

CREATE INDEX idx_skills_monster_id ON skills (monster_id);
CREATE INDEX idx_invocation_buffer_status ON invocation_buffer (status);
