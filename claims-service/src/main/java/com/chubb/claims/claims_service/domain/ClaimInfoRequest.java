package com.chubb.claims.claims_service.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_info_requests")
public class ClaimInfoRequest {

    @Id
    private UUID id;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(columnDefinition = "text")
    private String response;

    @Column(name = "responded_by")
    private String respondedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    protected ClaimInfoRequest() {}

    public static ClaimInfoRequest open(UUID claimId, String officerId, String question) {
        ClaimInfoRequest r = new ClaimInfoRequest();
        r.id = UUID.randomUUID();
        r.claimId = claimId;
        r.requestedBy = officerId;
        r.question = question;
        r.requestedAt = Instant.now();
        return r;
    }

    public void respond(String claimantId, String text) {
        if (response != null) {
            throw new ClaimConflictException("This information request has already been answered");
        }
        this.response = text;
        this.respondedBy = claimantId;
        this.respondedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getClaimId() { return claimId; }
    public String getRequestedBy() { return requestedBy; }
    public String getQuestion() { return question; }
    public String getResponse() { return response; }
    public String getRespondedBy() { return respondedBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getRespondedAt() { return respondedAt; }
}