package com.chubb.claims.claims_service.repository;

import com.chubb.claims.claims_service.domain.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, UUID> {}