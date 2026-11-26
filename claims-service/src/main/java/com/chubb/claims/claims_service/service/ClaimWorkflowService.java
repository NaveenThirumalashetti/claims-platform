package com.chubb.claims.claims_service.service;

import com.chubb.claims.claims_service.domain.*;
import com.chubb.claims.claims_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimWorkflowService {

    private final ClaimRepository claims;
    private final ClaimStatusHistoryRepository history;
    private final ClaimInfoRequestRepository infoRequests;

    public ClaimWorkflowService(ClaimRepository claims, ClaimStatusHistoryRepository history,
                                ClaimInfoRequestRepository infoRequests) {
        this.claims = claims;
        this.history = history;
        this.infoRequests = infoRequests;
    }

    @Transactional
    public Claim pickUp(UUID id, String officerId) {
        Claim c = load(id);
        ClaimStatus prev = c.pickUp(officerId);
        audit(c, prev, officerId, "Picked up by " + officerId);
        return c;
    }

    @Transactional
    public Claim assess(UUID id, String officerId, BigDecimal reserve) {
        Claim c = loadAssigned(id, officerId);
        ClaimStatus prev = c.assess(reserve);
        audit(c, prev, officerId, "Assessed, reserve " + reserve + " " + c.getCurrency());
        return c;
    }

    @Transactional
    public Claim approve(UUID id, String officerId) { return move(id, officerId, ClaimStatus.APPROVED, "Approved"); }

    @Transactional
    public Claim reject(UUID id, String officerId, String reason) { return move(id, officerId, ClaimStatus.REJECTED, reason); }

    @Transactional
    public Claim settle(UUID id, String officerId) { return move(id, officerId, ClaimStatus.SETTLED, "Settled"); }

    @Transactional
    public ClaimInfoRequest requestInfo(UUID id, String officerId, String question) {
        Claim c = loadAssigned(id, officerId);
        ClaimStatus prev = c.changeStatus(ClaimStatus.INFO_REQUESTED);
        ClaimInfoRequest r = infoRequests.save(ClaimInfoRequest.open(c.getId(), officerId, question));
        audit(c, prev, officerId, "Information requested");
        return r;
    }

    @Transactional
    public ClaimInfoRequest respond(UUID id, UUID requestId, String claimantId, String text) {
        Claim c = load(id);
        if (!c.getClaimantId().equals(claimantId)) {
            throw new ClaimAccessDeniedException("Only the claimant can respond to this request");
        }
        ClaimInfoRequest r = infoRequests.findByIdAndClaimId(requestId, id)
                .orElseThrow(() -> new InfoRequestNotFoundException(requestId));
        ClaimStatus prev = c.changeStatus(ClaimStatus.UNDER_REVIEW);   // fails unless INFO_REQUESTED
        r.respond(claimantId, text);
        audit(c, prev, claimantId, "Claimant provided information");
        return r;
    }

    @Transactional(readOnly = true)
    public List<ClaimInfoRequest> listInfoRequests(UUID id) {
        load(id);
        return infoRequests.findByClaimIdOrderByRequestedAtAsc(id);
    }

    private Claim move(UUID id, String officerId, ClaimStatus next, String reason) {
        Claim c = loadAssigned(id, officerId);
        ClaimStatus prev = c.changeStatus(next);
        audit(c, prev, officerId, reason);
        return c;
    }

    private Claim load(UUID id) {
        return claims.findById(id).orElseThrow(() -> new ClaimNotFoundException(id));
    }

    private Claim loadAssigned(UUID id, String officerId) {
        Claim c = load(id);
        if (!officerId.equals(c.getAssignedOfficerId())) {
            throw new ClaimAccessDeniedException("Claim is not assigned to " + officerId);
        }
        return c;
    }

    private void audit(Claim c, ClaimStatus from, String by, String reason) {
        history.save(ClaimStatusHistory.of(c.getId(), from, c.getStatus(), by, reason));
    }
}