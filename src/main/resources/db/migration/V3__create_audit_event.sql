-- Audit log foundation. Captures who-did-what-to-which-target for sensitive admin operations
-- (role grants/revokes, funeral state transitions, affiliate creation/deletion, user activation).
-- Wiring to the use cases that produce these events lands in the follow-up PR; this migration
-- only creates the storage and the indexes the read API will rely on.
CREATE TABLE audit_event (
    id              BIGSERIAL                NOT NULL,
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_email     VARCHAR(255)             NOT NULL,
    actor_id        BIGINT,
    action          VARCHAR(64)              NOT NULL,
    target_type     VARCHAR(64)              NOT NULL,
    target_id       VARCHAR(128)             NOT NULL,
    trace_id        VARCHAR(128),
    correlation_id  VARCHAR(128),
    payload         TEXT,
    CONSTRAINT pk_audit_event PRIMARY KEY (id)
);

-- Time-ordered index for the default 'recent events' view of the read API.
CREATE INDEX idx_audit_event_occurred_at ON audit_event (occurred_at DESC);

-- Per-actor lookup: 'show every action user X performed in the last 30 days'.
CREATE INDEX idx_audit_event_actor_occurred_at ON audit_event (actor_email, occurred_at DESC);

-- Per-target lookup: 'show every event affecting funeral 42 in chronological order'.
CREATE INDEX idx_audit_event_target ON audit_event (target_type, target_id, occurred_at DESC);

-- Action filter for compliance reports: 'list every USER_ROLE_GRANTED in the audit window'.
CREATE INDEX idx_audit_event_action_occurred_at ON audit_event (action, occurred_at DESC);
