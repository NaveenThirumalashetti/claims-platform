package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.domain.ClaimType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmitClaimRequest(
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "must be a 2-letter market code") String market,
        @NotNull ClaimType claimType,
        @NotBlank String claimantId,            // TEMPORARY: will come from the JWT later
        @NotBlank String policyNumber,
        @NotNull @PastOrPresent LocalDate incidentDate,
        @NotBlank @Size(max = 4000) String description,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal claimedAmount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO currency code") String currency) {}