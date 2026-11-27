package com.chubb.claims.claims_service.service;

import com.chubb.claims.claims_service.api.SubmitClaimRequest;
import com.chubb.claims.claims_service.domain.*;
import com.chubb.claims.claims_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ClaimService {

    private final ClaimRepository claims;
    private final ClaimStatusHistoryRepository history;

    public ClaimService(ClaimRepository claims, ClaimStatusHistoryRepository history) {
        this.claims = claims;
        this.history = history;
    }

    @Transactional
    public Claim submit(SubmitClaimRequest r, String claimantId) {
        Claim claim = Claim.submit(r.market(), r.claimType(), claimantId, r.policyNumber(),
                r.incidentDate(), r.description(), r.claimedAmount(), r.currency());
        claims.save(claim);
        history.save(ClaimStatusHistory.of(claim.getId(), null, ClaimStatus.SUBMITTED,
                claimantId, "Claim submitted"));
        return claim;
    }

    /** Claimants only see their own claims; someone else's claim looks like it doesn't exist (404, no leak). */
    @Transactional(readOnly = true)
    public Claim getVisibleTo(UUID id, String userId, boolean claimantOnly) {
        Claim c = claims.findById(id).orElseThrow(() -> new ClaimNotFoundException(id));
        if (claimantOnly && !c.getClaimantId().equals(userId)) {
            throw new ClaimNotFoundException(id);
        }
        return c;
    }
}