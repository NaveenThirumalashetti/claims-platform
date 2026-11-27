package com.chubb.claims.claims_service.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** DEMO ONLY: hardcoded users, all with password "password". A real system would use an IdP / user table. */
@Component
public class UserDirectory {

    public record AppUser(String id, String passwordHash, String role) {}

    private final Map<String, AppUser> users;
    private final PasswordEncoder encoder;

    public UserDirectory(PasswordEncoder encoder) {
        this.encoder = encoder;
        String hash = encoder.encode("password");
        users = Map.of(
                "claimant-1", new AppUser("claimant-1", hash, "CLAIMANT"),
                "claimant-2", new AppUser("claimant-2", hash, "CLAIMANT"),
                "officer-1", new AppUser("officer-1", hash, "OFFICER"),
                "officer-2", new AppUser("officer-2", hash, "OFFICER"),
                "manager-1", new AppUser("manager-1", hash, "MANAGER"));
    }

    public Optional<AppUser> authenticate(String username, String password) {
        return Optional.ofNullable(users.get(username))
                .filter(u -> encoder.matches(password, u.passwordHash()));
    }
}