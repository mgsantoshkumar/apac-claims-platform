package com.chubb.apac.claims.repository;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.domain.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    List<Claim> findByClaimantUserIdOrderByCreatedAtDesc(String claimantUserId);

    List<Claim> findByAdjusterUserIdOrderByUpdatedAtDesc(String adjusterUserId);

    List<Claim> findByStatusOrderByCreatedAtAsc(ClaimStatus status);

    List<Claim> findAllByOrderByUpdatedAtDesc();

    Optional<Claim> findByClaimNumber(String claimNumber);

    @Query("select c.status, count(c) from Claim c group by c.status")
    List<Object[]> countByStatus();

    @Query("select c.market, count(c) from Claim c group by c.market")
    List<Object[]> countByMarket();

    @Query("select coalesce(sum(c.reserveAmount), 0) from Claim c where c.status not in ('SETTLED', 'REJECTED')")
    java.math.BigDecimal sumOpenReserves();
}
