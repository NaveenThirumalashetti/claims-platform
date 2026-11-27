package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.domain.ClaimInfoRequest;
import com.chubb.claims.claims_service.service.ClaimWorkflowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{id}")
public class ClaimWorkflowController {

    public record AssessRequest(@NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal reserveAmount) {}
    public record RejectRequest(@NotBlank String reason) {}
    public record InfoRequestRequest(@NotBlank String question) {}
    public record InfoResponseRequest(@NotBlank String response) {}
    public record InfoRequestView(UUID id, String requestedBy, String question, String response,
                                  String respondedBy, Instant requestedAt, Instant respondedAt) {
        static InfoRequestView from(ClaimInfoRequest r) {
            return new InfoRequestView(r.getId(), r.getRequestedBy(), r.getQuestion(), r.getResponse(),
                    r.getRespondedBy(), r.getRequestedAt(), r.getRespondedAt());
        }
    }

    private final ClaimWorkflowService service;

    public ClaimWorkflowController(ClaimWorkflowService service) { this.service = service; }

    @PostMapping("/pick-up")
    public ClaimResponse pickUp(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ClaimResponse.from(service.pickUp(id, jwt.getSubject()));
    }

    @PostMapping("/assess")
    public ClaimResponse assess(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody AssessRequest body) {
        return ClaimResponse.from(service.assess(id, jwt.getSubject(), body.reserveAmount()));
    }

    @PostMapping("/approve")
    public ClaimResponse approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ClaimResponse.from(service.approve(id, jwt.getSubject()));
    }

    @PostMapping("/reject")
    public ClaimResponse reject(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody RejectRequest body) {
        return ClaimResponse.from(service.reject(id, jwt.getSubject(), body.reason()));
    }

    @PostMapping("/settle")
    public ClaimResponse settle(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ClaimResponse.from(service.settle(id, jwt.getSubject()));
    }

    @PostMapping("/info-requests")
    public InfoRequestView requestInfo(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody InfoRequestRequest body) {
        return InfoRequestView.from(service.requestInfo(id, jwt.getSubject(), body.question()));
    }

    @PostMapping("/info-requests/{requestId}/response")
    public InfoRequestView respond(@PathVariable UUID id, @PathVariable UUID requestId,
                                   @AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody InfoResponseRequest body) {
        return InfoRequestView.from(service.respond(id, requestId, jwt.getSubject(), body.response()));
    }

    @GetMapping("/info-requests")
    public List<InfoRequestView> infoRequests(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        boolean claimant = "CLAIMANT".equals(jwt.getClaimAsString("role"));
        return service.listInfoRequests(id, jwt.getSubject(), claimant).stream()
                .map(InfoRequestView::from).toList();
    }
}