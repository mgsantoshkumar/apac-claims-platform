package com.chubb.apac.notifications.web.dto;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.notifications.domain.TimelineEntry;

import java.time.Instant;
import java.util.UUID;

public record TimelineEntryResponse(
        UUID id,
        UUID claimId,
        String claimNumber,
        ClaimEventType eventType,
        ClaimStatus status,
        String actorUserId,
        String message,
        Instant occurredAt
) {
    public static TimelineEntryResponse from(TimelineEntry entry) {
        return new TimelineEntryResponse(
                entry.getId(),
                entry.getClaimId(),
                entry.getClaimNumber(),
                entry.getEventType(),
                entry.getStatus(),
                entry.getActorUserId(),
                entry.getMessage(),
                entry.getOccurredAt()
        );
    }
}
