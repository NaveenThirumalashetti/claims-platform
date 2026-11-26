package com.chubb.claims.claims_service.domain;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ClaimStatus from, ClaimStatus to) {
        super("Cannot move claim from " + from + " to " + to);
    }
}