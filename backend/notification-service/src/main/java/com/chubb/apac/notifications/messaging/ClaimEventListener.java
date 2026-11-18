package com.chubb.apac.notifications.messaging;

import com.chubb.apac.claims.common.events.ClaimDomainEvent;
import com.chubb.apac.claims.common.events.ClaimTopics;
import com.chubb.apac.notifications.service.ProjectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClaimEventListener {

    private final ProjectionService projectionService;

    public ClaimEventListener(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @KafkaListener(topics = {
            ClaimTopics.CLAIM_SUBMITTED,
            ClaimTopics.CLAIM_ASSIGNED,
            ClaimTopics.CLAIM_INFO_REQUESTED,
            ClaimTopics.CLAIM_INFO_PROVIDED,
            ClaimTopics.CLAIM_DECIDED
    })
    public void onEvent(ClaimDomainEvent event) {
        projectionService.project(event);
    }
}
