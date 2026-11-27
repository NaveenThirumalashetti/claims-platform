package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.domain.ClaimStatus;
import com.chubb.claims.claims_service.service.ClaimQueryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimQueryController {

    public record PagedClaims(List<ClaimResponse> items, int page, int size,
                              long totalElements, int totalPages) {}

    private final ClaimQueryService service;

    public ClaimQueryController(ClaimQueryService service) { this.service = service; }

    @GetMapping
    public PagedClaims search(@RequestParam(required = false) ClaimStatus status,
                              @RequestParam(required = false) String assignedTo,
                              @RequestParam(defaultValue = "false") boolean unassigned,
                              @RequestParam(required = false) String market,
                              @RequestParam(required = false) String claimantId,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        var result = service.search(status, assignedTo, unassigned, market, claimantId, page, size);
        return new PagedClaims(result.getContent().stream().map(ClaimResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/mine")
    public PagedClaims mine(@AuthenticationPrincipal Jwt jwt,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        var result = service.search(null, null, false, null, jwt.getSubject(), page, size);
        return new PagedClaims(result.getContent().stream().map(ClaimResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}