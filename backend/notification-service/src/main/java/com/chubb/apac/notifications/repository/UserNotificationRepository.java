package com.chubb.apac.notifications.repository;

import com.chubb.apac.notifications.domain.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(String userId);
}
