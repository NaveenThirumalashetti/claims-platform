package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService service;

    public ClaimController(ClaimService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ClaimResponse> submit(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody SubmitClaimRequest request) {
        ClaimResponse body = ClaimResponse.from(service.submit(request, jwt.getSubject()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(body.id()).toUri();
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/{id}")
    public ClaimResponse get(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        boolean claimant = "CLAIMANT".equals(jwt.getClaimAsString("role"));
        return ClaimResponse.from(service.getVisibleTo(id, jwt.getSubject(), claimant));
    }
}