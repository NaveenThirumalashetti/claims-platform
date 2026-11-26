package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimResponse(UUID id, String claimNumber, String market, ClaimType claimType,
                            ClaimStatus status, String policyNumber, LocalDate incidentDate,
                            String description, BigDecimal claimedAmount, String currency,
                            Instant createdAt, Instant updatedAt) {

    public static ClaimResponse from(Claim c) {
        return new ClaimResponse(c.getId(), c.getClaimNumber(), c.getMarket(), c.getClaimType(),
                c.getStatus(), c.getPolicyNumber(), c.getIncidentDate(), c.getDescription(),
                c.getClaimedAmount(), c.getCurrency(), c.getCreatedAt(), c.getUpdatedAt());
    }
}