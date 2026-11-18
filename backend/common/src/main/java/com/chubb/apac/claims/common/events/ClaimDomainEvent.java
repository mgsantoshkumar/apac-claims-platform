package com.chubb.apac.claims.common.events;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Canonical domain event published by claims-service and consumed by other modules.
 */
public record ClaimDomainEvent(
        String eventId,
        ClaimEventType eventType,
        UUID claimId,
        String claimNumber,
        ClaimStatus status,
        Market market,
        Lob lob,
        String claimantUserId,
        String adjusterUserId,
        String actorUserId,
        String message,
        BigDecimal reserveAmount,
        BigDecimal settlementAmount,
        Instant occurredAt
) {
    public static ClaimDomainEvent of(
            ClaimEventType type,
            UUID claimId,
            String claimNumber,
            ClaimStatus status,
            Market market,
            Lob lob,
            String claimantUserId,
            String adjusterUserId,
            String actorUserId,
            String message,
            BigDecimal reserveAmount,
            BigDecimal settlementAmount
    ) {
        return new ClaimDomainEvent(
                UUID.randomUUID().toString(),
                type,
                claimId,
                claimNumber,
                status,
                market,
                lob,
                claimantUserId,
                adjusterUserId,
                actorUserId,
                message,
                reserveAmount,
                settlementAmount,
                Instant.now()
        );
    }
}
