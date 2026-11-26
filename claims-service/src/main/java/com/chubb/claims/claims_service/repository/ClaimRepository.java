package com.chubb.claims.claims_service.repository;

import com.chubb.claims.claims_service.domain.Claim;
import com.chubb.claims.claims_service.domain.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID>, JpaSpecificationExecutor<Claim> {

    interface WorkloadRow {
        String getOfficerId();
        ClaimStatus getStatus();
        long getCount();
        Instant getOldest();
    }

    interface ExposureRow {
        String getMarket();
        String getCurrency();
        long getOpenClaims();
        BigDecimal getExposure();
    }

    @Query("""
            select c.assignedOfficerId as officerId, c.status as status,
                   count(c) as count, min(c.createdAt) as oldest
            from Claim c
            where c.assignedOfficerId is not null and c.status not in :terminal
            group by c.assignedOfficerId, c.status
            """)
    List<WorkloadRow> workload(@Param("terminal") Collection<ClaimStatus> terminal);

    /** Exposure = reserve if the claim has been assessed, otherwise the claimed amount. */
    @Query("""
            select c.market as market, c.currency as currency, count(c) as openClaims,
                   sum(coalesce(c.reserveAmount, c.claimedAmount)) as exposure
            from Claim c
            where c.status not in :terminal
            group by c.market, c.currency
            order by c.market, c.currency
            """)
    List<ExposureRow> exposure(@Param("terminal") Collection<ClaimStatus> terminal);

    long countByAssignedOfficerIdIsNullAndStatus(ClaimStatus status);
}