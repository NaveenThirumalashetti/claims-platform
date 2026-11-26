CREATE TABLE claim_info_requests (
    id            UUID PRIMARY KEY,
    claim_id      UUID NOT NULL REFERENCES claims(id),
    requested_by  VARCHAR(64) NOT NULL,
    question      TEXT NOT NULL,
    response      TEXT,
    responded_by  VARCHAR(64),
    requested_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at  TIMESTAMPTZ
);
CREATE INDEX idx_info_requests_claim ON claim_info_requests(claim_id);