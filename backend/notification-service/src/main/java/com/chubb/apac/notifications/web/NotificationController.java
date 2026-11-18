package com.chubb.apac.notifications.web;

import com.chubb.apac.notifications.repository.TimelineEntryRepository;
import com.chubb.apac.notifications.repository.UserNotificationRepository;
import com.chubb.apac.notifications.security.AuthenticatedUser;
import com.chubb.apac.notifications.web.dto.NotificationResponse;
import com.chubb.apac.notifications.web.dto.TimelineEntryResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final TimelineEntryRepository timelineEntryRepository;
    private final UserNotificationRepository userNotificationRepository;

    public NotificationController(
            TimelineEntryRepository timelineEntryRepository,
            UserNotificationRepository userNotificationRepository
    ) {
        this.timelineEntryRepository = timelineEntryRepository;
        this.userNotificationRepository = userNotificationRepository;
    }

    @GetMapping("/claims/{claimId}/timeline")
    public List<TimelineEntryResponse> timeline(@PathVariable UUID claimId) {
        currentUser();
        return timelineEntryRepository.findByClaimIdOrderByOccurredAtAsc(claimId).stream()
                .map(TimelineEntryResponse::from)
                .toList();
    }

    @GetMapping("/notifications/me")
    public List<NotificationResponse> myNotifications() {
        AuthenticatedUser user = currentUser();
        return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(user.userId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    private AuthenticatedUser currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Unauthenticated");
        }
        return user;
    }
}
