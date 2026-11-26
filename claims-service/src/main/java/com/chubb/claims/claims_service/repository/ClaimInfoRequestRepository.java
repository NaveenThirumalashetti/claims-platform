package com.chubb.claims.claims_service.repository;

import com.chubb.claims.claims_service.domain.ClaimInfoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ClaimInfoRequestRepository extends JpaRepository<ClaimInfoRequest, UUID> {
    Optional<ClaimInfoRequest> findByIdAndClaimId(UUID id, UUID claimId);
    List<ClaimInfoRequest> findByClaimIdOrderByRequestedAtAsc(UUID claimId);
}