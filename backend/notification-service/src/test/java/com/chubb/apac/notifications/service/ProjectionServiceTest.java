package com.chubb.apac.notifications.service;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.enums.Lob;
import com.chubb.apac.claims.common.enums.Market;
import com.chubb.apac.claims.common.events.ClaimDomainEvent;
import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.notifications.repository.TimelineEntryRepository;
import com.chubb.apac.notifications.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectionServiceTest {

    @Mock
    TimelineEntryRepository timelineEntryRepository;

    @Mock
    UserNotificationRepository userNotificationRepository;

    @InjectMocks
    ProjectionService projectionService;

    @Test
    void projectPersistsTimelineAndNotifications() {
        ClaimDomainEvent event = ClaimDomainEvent.of(
                ClaimEventType.SUBMITTED,
                UUID.randomUUID(),
                "CLM-SG-2026-1001",
                ClaimStatus.SUBMITTED,
                Market.SG,
                Lob.MOTOR,
                "u-claimant-1",
                null,
                "u-claimant-1",
                "Claim submitted",
                new BigDecimal("1000"),
                null
        );
        when(timelineEntryRepository.existsByClaimIdAndEventTypeAndOccurredAt(any(), any(), any()))
                .thenReturn(false);

        projectionService.project(event);

        verify(timelineEntryRepository).save(any());
        ArgumentCaptor<com.chubb.apac.notifications.domain.UserNotification> captor =
                ArgumentCaptor.forClass(com.chubb.apac.notifications.domain.UserNotification.class);
        verify(userNotificationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("u-claimant-1");
    }

    @Test
    void projectSkipsDuplicates() {
        ClaimDomainEvent event = ClaimDomainEvent.of(
                ClaimEventType.ASSIGNED,
                UUID.randomUUID(),
                "CLM-SG-2026-1001",
                ClaimStatus.ASSIGNED,
                Market.SG,
                Lob.MOTOR,
                "u-claimant-1",
                "u-adjuster-1",
                "u-adjuster-1",
                "Assigned",
                new BigDecimal("1000"),
                null
        );
        when(timelineEntryRepository.existsByClaimIdAndEventTypeAndOccurredAt(any(), any(), any()))
                .thenReturn(true);

        projectionService.project(event);

        verify(timelineEntryRepository, never()).save(any());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void assignedEventNotifiesClaimantAndAdjuster() {
        ClaimDomainEvent event = ClaimDomainEvent.of(
                ClaimEventType.ASSIGNED,
                UUID.randomUUID(),
                "CLM-SG-2026-1002",
                ClaimStatus.ASSIGNED,
                Market.SG,
                Lob.MOTOR,
                "u-claimant-1",
                "u-adjuster-1",
                "u-adjuster-1",
                "Claim assigned to adjuster",
                new BigDecimal("1000"),
                null
        );
        when(timelineEntryRepository.existsByClaimIdAndEventTypeAndOccurredAt(any(), any(), any()))
                .thenReturn(false);

        projectionService.project(event);

        ArgumentCaptor<com.chubb.apac.notifications.domain.UserNotification> captor =
                ArgumentCaptor.forClass(com.chubb.apac.notifications.domain.UserNotification.class);
        verify(userNotificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(com.chubb.apac.notifications.domain.UserNotification::getUserId)
                .containsExactlyInAnyOrder("u-claimant-1", "u-adjuster-1");
        assertThat(captor.getAllValues().getFirst().getTitle()).contains("Claim assigned");
    }
}
