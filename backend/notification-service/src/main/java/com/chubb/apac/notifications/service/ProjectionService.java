package com.chubb.apac.notifications.service;

import com.chubb.apac.claims.common.events.ClaimDomainEvent;
import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.notifications.domain.TimelineEntry;
import com.chubb.apac.notifications.domain.UserNotification;
import com.chubb.apac.notifications.repository.TimelineEntryRepository;
import com.chubb.apac.notifications.repository.UserNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class ProjectionService {

    private static final Logger log = LoggerFactory.getLogger(ProjectionService.class);

    private final TimelineEntryRepository timelineEntryRepository;
    private final UserNotificationRepository userNotificationRepository;

    public ProjectionService(
            TimelineEntryRepository timelineEntryRepository,
            UserNotificationRepository userNotificationRepository
    ) {
        this.timelineEntryRepository = timelineEntryRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public void project(ClaimDomainEvent event) {
        if (timelineEntryRepository.existsByClaimIdAndEventTypeAndOccurredAt(
                event.claimId(), event.eventType(), event.occurredAt())) {
            log.debug("Skipping duplicate event {} for {}", event.eventType(), event.claimNumber());
            return;
        }

        TimelineEntry entry = new TimelineEntry();
        entry.setClaimId(event.claimId());
        entry.setClaimNumber(event.claimNumber());
        entry.setEventType(event.eventType());
        entry.setStatus(event.status());
        entry.setActorUserId(event.actorUserId());
        entry.setMessage(event.message());
        entry.setOccurredAt(event.occurredAt());
        timelineEntryRepository.save(entry);

        for (String userId : recipients(event)) {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setClaimId(event.claimId());
            notification.setClaimNumber(event.claimNumber());
            notification.setEventType(event.eventType());
            notification.setTitle(titleFor(event.eventType(), event.claimNumber()));
            notification.setBody(event.message() == null ? event.eventType().name() : event.message());
            notification.setReadFlag(false);
            notification.setCreatedAt(event.occurredAt());
            userNotificationRepository.save(notification);
        }

        log.info("Projected {} for claim {}", event.eventType(), event.claimNumber());
    }

    private Set<String> recipients(ClaimDomainEvent event) {
        Set<String> users = new LinkedHashSet<>();
        if (event.claimantUserId() != null) {
            users.add(event.claimantUserId());
        }
        if (event.adjusterUserId() != null) {
            users.add(event.adjusterUserId());
        }
        return users;
    }

    private String titleFor(ClaimEventType type, String claimNumber) {
        return switch (type) {
            case SUBMITTED -> "Claim submitted: " + claimNumber;
            case ASSIGNED -> "Claim assigned: " + claimNumber;
            case INFO_REQUESTED -> "Information requested: " + claimNumber;
            case INFO_PROVIDED -> "Information provided: " + claimNumber;
            case DECIDED -> "Decision recorded: " + claimNumber;
        };
    }
}
