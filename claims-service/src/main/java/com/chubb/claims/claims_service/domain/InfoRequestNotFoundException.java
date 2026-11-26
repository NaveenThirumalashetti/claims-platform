package com.chubb.claims.claims_service.service;

import java.util.UUID;

public class InfoRequestNotFoundException extends RuntimeException {
    public InfoRequestNotFoundException(UUID id) { super("Info request not found: " + id); }
}