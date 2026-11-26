package com.chubb.claims.claims_service.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_status_history")
public class ClaimStatusHistory {

    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private ClaimStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private ClaimStatus toStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected ClaimStatusHistory() {}

    public static ClaimStatusHistory of(UUID claimId, ClaimStatus from, ClaimStatus to,
                                        String changedBy, String reason) {
        ClaimStatusHistory h = new ClaimStatusHistory();
        h.id = UUID.randomUUID();
        h.claimId = claimId;
        h.fromStatus = from;
        h.toStatus = to;
        h.changedBy = changedBy;
        h.reason = reason;
        h.changedAt = Instant.now();
        return h;
    }
}