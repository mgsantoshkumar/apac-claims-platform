package com.chubb.apac.notifications.domain;

import com.chubb.apac.claims.common.enums.ClaimStatus;
import com.chubb.apac.claims.common.events.ClaimEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline_entries", schema = "notifications")
public class TimelineEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID claimId;

    @Column(nullable = false, length = 32)
    private String claimNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClaimEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ClaimStatus status;

    @Column(length = 64)
    private String actorUserId;

    @Column(length = 4000)
    private String message;

    @Column(nullable = false)
    private Instant occurredAt;

    public UUID getId() {
        return id;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public ClaimEventType getEventType() {
        return eventType;
    }

    public void setEventType(ClaimEventType eventType) {
        this.eventType = eventType;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
