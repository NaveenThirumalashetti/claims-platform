package com.chubb.claims.claims_service.service;

import com.chubb.claims.claims_service.domain.ClaimStatus;
import com.chubb.claims.claims_service.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class DashboardService {

    public record OfficerWorkload(String officerId, long openClaims,
                                  Map<ClaimStatus, Long> byStatus, Instant oldestOpenClaim) {}
    public record WorkloadSummary(long unassignedClaims, List<OfficerWorkload> officers) {}
    public record ExposureLine(String market, String currency, long openClaims, BigDecimal exposure) {}

    private final ClaimRepository claims;

    public DashboardService(ClaimRepository claims) { this.claims = claims; }

    @Transactional(readOnly = true)
    public WorkloadSummary workload() {
        Map<String, Map<ClaimStatus, Long>> byOfficer = new TreeMap<>();
        Map<String, Instant> oldest = new HashMap<>();

        for (var row : claims.workload(ClaimStatus.terminalStates())) {
            byOfficer.computeIfAbsent(row.getOfficerId(), k -> new EnumMap<>(ClaimStatus.class))
                    .put(row.getStatus(), row.getCount());
            oldest.merge(row.getOfficerId(), row.getOldest(),
                    (a, b) -> a.isBefore(b) ? a : b);
        }

        List<OfficerWorkload> officers = byOfficer.entrySet().stream()
                .map(e -> new OfficerWorkload(e.getKey(),
                        e.getValue().values().stream().mapToLong(Long::longValue).sum(),
                        e.getValue(), oldest.get(e.getKey())))
                .toList();

        long unassigned = claims.countByAssignedOfficerIdIsNullAndStatus(ClaimStatus.SUBMITTED);
        return new WorkloadSummary(unassigned, officers);
    }

    @Transactional(readOnly = true)
    public List<ExposureLine> exposure() {
        return claims.exposure(ClaimStatus.terminalStates()).stream()
                .map(r -> new ExposureLine(r.getMarket(), r.getCurrency(), r.getOpenClaims(), r.getExposure()))
                .toList();
    }
}