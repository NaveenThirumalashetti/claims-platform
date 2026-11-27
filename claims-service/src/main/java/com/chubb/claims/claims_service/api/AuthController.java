package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.security.UserDirectory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String token, String userId, String role, long expiresInSeconds) {}

    private final UserDirectory users;
    private final JwtEncoder encoder;
    private final long ttl;

    public AuthController(UserDirectory users, JwtEncoder encoder,
                          @Value("${app.jwt.ttl-seconds}") long ttl) {
        this.users = users;
        this.encoder = encoder;
        this.ttl = ttl;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        var user = users.authenticate(req.username(), req.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("claims-service")
                .subject(user.id())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttl))
                .claim("role", user.role())
                .build();
        String token = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, user.id(), user.role(), ttl);
    }
}