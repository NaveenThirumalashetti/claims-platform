package com.chubb.claims.claims_service.repository;

import com.chubb.claims.claims_service.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {}