package com.chubb.claims.claims_service.service;

import java.util.UUID;

public class ClaimNotFoundException extends RuntimeException {
    public ClaimNotFoundException(UUID id) { super("Claim not found: " + id); }
}