-- V1: notifications table
-- Owner of this schema: notification-service (ADR-004). Never edit this file once applied;
-- create a new migration (V2__...) instead.
-- Design: 09-microservices/services/03-notification-service/data-model.md

CREATE TABLE notifications (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID          NOT NULL,          -- user from auth-service: no FK, it lives in another database
    type             VARCHAR(50)   NOT NULL,          -- e.g. WELCOME; alert types to be decided
    channel          VARCHAR(20)   NOT NULL,          -- e.g. EMAIL; channels to be decided (DEC-004)
    recipient        VARCHAR(255)  NOT NULL,          -- address used at sending time
    subject          VARCHAR(255)  NOT NULL,
    message          TEXT          NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    error_message    TEXT,                            -- reason of the last failure
    source_event_id  UUID,                            -- eventId of the consumed event (idempotency, DEC-003)
    sent_at          TIMESTAMPTZ,                     -- NULL until SENT
    read_at          TIMESTAMPTZ,                     -- NULL = unread
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ,                     -- soft delete: NULL = active

    CONSTRAINT ck_notifications_status CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT uq_notifications_source_event_id UNIQUE (source_event_id)
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_status ON notifications (status);
