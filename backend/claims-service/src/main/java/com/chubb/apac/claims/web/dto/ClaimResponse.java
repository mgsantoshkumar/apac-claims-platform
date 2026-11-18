package com.chubb.apac.claims.web.dto;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
import com.chubb.apac.claims.domain.Claim;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        String claimNumber,
        Market market,
        Lob lob,
        ClaimStatus status,
        String claimantUserId,
        String adjusterUserId,
        String incidentSummary,
        String incidentDescription,
        LocalDate incidentDate,
        String location,
        BigDecimal estimatedLoss,
        BigDecimal reserveAmount,
        BigDecimal settlementAmount,
        String infoRequestMessage,
        String infoResponseMessage,
        String decisionReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getMarket(),
                claim.getLob(),
                claim.getStatus(),
                claim.getClaimantUserId(),
                claim.getAdjusterUserId(),
                claim.getIncidentSummary(),
                claim.getIncidentDescription(),
                claim.getIncidentDate(),
                claim.getLocation(),
                claim.getEstimatedLoss(),
                claim.getReserveAmount(),
                claim.getSettlementAmount(),
                claim.getInfoRequestMessage(),
                claim.getInfoResponseMessage(),
                claim.getDecisionReason(),
                claim.getCreatedAt(),
                claim.getUpdatedAt()
        );
    }
}
