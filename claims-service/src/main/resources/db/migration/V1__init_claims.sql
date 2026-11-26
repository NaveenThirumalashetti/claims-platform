CREATE TABLE claims (
    id                  UUID PRIMARY KEY,
    claim_number        VARCHAR(30) NOT NULL UNIQUE,
    market              VARCHAR(2)  NOT NULL,      -- e.g. SG, HK, TH
    claim_type          VARCHAR(20) NOT NULL,      -- MOTOR / PROPERTY
    status              VARCHAR(20) NOT NULL,
    claimant_id         VARCHAR(64) NOT NULL,
    policy_number       VARCHAR(40) NOT NULL,
    incident_date       DATE        NOT NULL,
    description         TEXT        NOT NULL,
    claimed_amount      NUMERIC(14,2) NOT NULL,
    reserve_amount      NUMERIC(14,2),
    currency            CHAR(3)     NOT NULL,
    assigned_officer_id VARCHAR(64),
    version             BIGINT      NOT NULL DEFAULT 0,  -- optimistic locking
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE claim_status_history (
    id          UUID PRIMARY KEY,
    claim_id    UUID NOT NULL REFERENCES claims(id),
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    changed_by  VARCHAR(64) NOT NULL,
    reason      TEXT,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_officer ON claims(assigned_officer_id);
CREATE INDEX idx_claims_claimant ON claims(claimant_id);