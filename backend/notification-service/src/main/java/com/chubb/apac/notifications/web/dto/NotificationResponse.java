package com.chubb.apac.notifications.web.dto;

import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.notifications.domain.UserNotification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String userId,
        UUID claimId,
        String claimNumber,
        ClaimEventType eventType,
        String title,
        String body,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(UserNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getClaimId(),
                notification.getClaimNumber(),
                notification.getEventType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isReadFlag(),
                notification.getCreatedAt()
        );
    }
}
