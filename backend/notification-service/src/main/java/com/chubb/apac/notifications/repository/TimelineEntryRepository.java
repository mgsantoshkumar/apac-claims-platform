package com.chubb.apac.notifications.repository;

import com.chubb.apac.notifications.domain.TimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimelineEntryRepository extends JpaRepository<TimelineEntry, UUID> {
    List<TimelineEntry> findByClaimIdOrderByOccurredAtAsc(UUID claimId);

    boolean existsByClaimIdAndEventTypeAndOccurredAt(
            UUID claimId,
            com.chubb.apac.claims.common.events.ClaimEventType eventType,
            java.time.Instant occurredAt
    );
}
