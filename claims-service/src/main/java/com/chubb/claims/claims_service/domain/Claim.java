package com.chubb.claims.claims_service.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
public class Claim {

    @Id
    private UUID id;

    @Column(name = "claim_number", nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false, length = 2)
    private String market;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(name = "claimant_id", nullable = false)
    private String claimantId;

    @Column(name = "policy_number", nullable = false)
    private String policyNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "claimed_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "reserve_amount", precision = 14, scale = 2)
    private BigDecimal reserveAmount;

    @JdbcTypeCode(SqlTypes.CHAR)          // column is CHAR(3); needed for ddl-auto=validate
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "assigned_officer_id")
    private String assignedOfficerId;

    @Version
    private Long version;                  // optimistic locking

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Claim() {}                   // required by JPA

    public static Claim submit(String market, ClaimType type, String claimantId, String policyNumber,
                               LocalDate incidentDate, String description,
                               BigDecimal claimedAmount, String currency) {
        Claim c = new Claim();
        c.id = UUID.randomUUID();
        c.claimNumber = "CLM-" + market + "-" + c.id.toString().substring(0, 8).toUpperCase();
        c.market = market;
        c.claimType = type;
        c.claimantId = claimantId;
        c.policyNumber = policyNumber;
        c.incidentDate = incidentDate;
        c.description = description;
        c.claimedAmount = claimedAmount;
        c.currency = currency;
        c.status = ClaimStatus.SUBMITTED;
        return c;
    }

    /** All status changes go through here so the state machine can't be bypassed. */
    public ClaimStatus changeStatus(ClaimStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(status, next);
        }
        ClaimStatus previous = status;
        this.status = next;
        return previous;
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getClaimNumber() { return claimNumber; }
    public String getMarket() { return market; }
    public ClaimType getClaimType() { return claimType; }
    public ClaimStatus getStatus() { return status; }
    public String getClaimantId() { return claimantId; }
    public String getPolicyNumber() { return policyNumber; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public String getDescription() { return description; }
    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public BigDecimal getReserveAmount() { return reserveAmount; }
    public String getCurrency() { return currency; }
    public String getAssignedOfficerId() { return assignedOfficerId; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}