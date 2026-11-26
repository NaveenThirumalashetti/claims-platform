package com.chubb.claims.claims_service.domain;

public class ClaimAccessDeniedException extends RuntimeException {
    public ClaimAccessDeniedException(String message) { super(message); }
}