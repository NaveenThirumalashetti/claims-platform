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
    public Claim submit(SubmitClaimRequest r) {
        Claim claim = Claim.submit(r.market(), r.claimType(), r.claimantId(), r.policyNumber(),
                r.incidentDate(), r.description(), r.claimedAmount(), r.currency());
        claims.save(claim);
        history.save(ClaimStatusHistory.of(claim.getId(), null, ClaimStatus.SUBMITTED,
                r.claimantId(), "Claim submitted"));
        return claim;
    }

    @Transactional(readOnly = true)
    public Claim get(UUID id) {
        return claims.findById(id).orElseThrow(() -> new ClaimNotFoundException(id));
    }
}