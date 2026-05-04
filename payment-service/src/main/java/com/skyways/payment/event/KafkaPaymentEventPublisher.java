package com.skyways.payment.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyways.payment.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "skyways.kafka.enabled", havingValue = "true")
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaPaymentEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaPaymentEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${skyways.kafka.topics.payment-events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(String eventType, Payment payment) {
        try {
            PaymentEvent event = PaymentEvent.from(eventType, payment);
            kafkaTemplate.send(topic, payment.getBookingId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException ex) {
            log.warn("Unable to serialize payment event {} for payment {}", eventType, payment.getId(), ex);
        }
    }
}
