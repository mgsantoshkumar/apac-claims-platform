package com.chubb.apac.claims.messaging;

import com.chubb.apac.claims.common.events.ClaimDomainEvent;
import com.chubb.apac.claims.common.events.ClaimEventType;
import com.chubb.apac.claims.common.events.ClaimTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClaimEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClaimEventPublisher.class);

    private final KafkaTemplate<String, ClaimDomainEvent> kafkaTemplate;

    public ClaimEventPublisher(KafkaTemplate<String, ClaimDomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ClaimDomainEvent event) {
        String topic = topicFor(event.eventType());
        kafkaTemplate.send(topic, event.claimId().toString(), event);
        log.info("Published {} to {} for claim {}", event.eventType(), topic, event.claimNumber());
    }

    private String topicFor(ClaimEventType type) {
        return switch (type) {
            case SUBMITTED -> ClaimTopics.CLAIM_SUBMITTED;
            case ASSIGNED -> ClaimTopics.CLAIM_ASSIGNED;
            case INFO_REQUESTED -> ClaimTopics.CLAIM_INFO_REQUESTED;
            case INFO_PROVIDED -> ClaimTopics.CLAIM_INFO_PROVIDED;
            case DECIDED -> ClaimTopics.CLAIM_DECIDED;
        };
    }
}
