package com.chubb.claims.claims_service.domain;

public class ClaimConflictException extends RuntimeException {
    public ClaimConflictException(String message) { super(message); }
}