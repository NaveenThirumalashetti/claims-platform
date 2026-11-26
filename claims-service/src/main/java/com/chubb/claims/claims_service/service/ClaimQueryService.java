package com.chubb.claims.claims_service.service;

import com.chubb.claims.claims_service.domain.Claim;
import com.chubb.claims.claims_service.domain.ClaimStatus;
import com.chubb.claims.claims_service.repository.ClaimRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimQueryService {

    private final ClaimRepository claims;

    public ClaimQueryService(ClaimRepository claims) { this.claims = claims; }

    @Transactional(readOnly = true)
    public Page<Claim> search(ClaimStatus status, String assignedTo, boolean unassigned,
                              String market, String claimantId, int page, int size) {
        Specification<Claim> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (status != null)     p.add(cb.equal(root.get("status"), status));
            if (assignedTo != null) p.add(cb.equal(root.get("assignedOfficerId"), assignedTo));
            if (unassigned)         p.add(cb.isNull(root.get("assignedOfficerId")));
            if (market != null)     p.add(cb.equal(root.get("market"), market));
            if (claimantId != null) p.add(cb.equal(root.get("claimantId"), claimantId));
            return cb.and(p.toArray(new Predicate[0]));
        };
        // Oldest first: the queue is worked from the top. Page size is capped to protect the DB.
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("createdAt").ascending());
        return claims.findAll(spec, pageable);
    }
}