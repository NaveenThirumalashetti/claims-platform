package com.chubb.claims.claims_service.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClaimStatusTest {

    @Test
    void happyPathIsAllowed() {
        assertTrue(ClaimStatus.SUBMITTED.canTransitionTo(ClaimStatus.UNDER_REVIEW));
        assertTrue(ClaimStatus.UNDER_REVIEW.canTransitionTo(ClaimStatus.ASSESSED));
        assertTrue(ClaimStatus.ASSESSED.canTransitionTo(ClaimStatus.APPROVED));
        assertTrue(ClaimStatus.APPROVED.canTransitionTo(ClaimStatus.SETTLED));
    }

    @Test
    void cannotSkipStages() {
        assertFalse(ClaimStatus.SUBMITTED.canTransitionTo(ClaimStatus.APPROVED));
        assertFalse(ClaimStatus.UNDER_REVIEW.canTransitionTo(ClaimStatus.SETTLED));
    }

    @Test
    void terminalStatesHaveNoExit() {
        assertTrue(ClaimStatus.REJECTED.allowedNext().isEmpty());
        assertTrue(ClaimStatus.SETTLED.allowedNext().isEmpty());
    }

    @Test
    void entityRejectsIllegalTransition() {
        Claim c = Claim.submit("SG", ClaimType.MOTOR, "u1", "POL-1",
                java.time.LocalDate.now(), "dent", java.math.BigDecimal.TEN, "SGD");
        assertThrows(InvalidStatusTransitionException.class, () -> c.changeStatus(ClaimStatus.SETTLED));
    }
}