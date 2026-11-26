package com.chubb.claims.claims_service.domain;

import java.util.Set;
import java.util.EnumSet;

public enum ClaimStatus {
    SUBMITTED, UNDER_REVIEW, INFO_REQUESTED, ASSESSED, APPROVED, REJECTED, SETTLED;

    public Set<ClaimStatus> allowedNext() {
        return switch (this) {
            case SUBMITTED      -> Set.of(UNDER_REVIEW);
            case UNDER_REVIEW   -> Set.of(INFO_REQUESTED, ASSESSED);
            case INFO_REQUESTED -> Set.of(UNDER_REVIEW);
            case ASSESSED       -> Set.of(APPROVED, REJECTED);
            case APPROVED       -> Set.of(SETTLED);
            case REJECTED, SETTLED -> Set.of();   // terminal
        };
    }

    public boolean canTransitionTo(ClaimStatus next) {
        return allowedNext().contains(next);
    }

    public boolean isTerminal() { return allowedNext().isEmpty(); }

    /** Statuses where the claim is finished and no liability remains open. */
    public static Set<ClaimStatus> terminalStates() { 
        return EnumSet.of(REJECTED, SETTLED); 
    }
}