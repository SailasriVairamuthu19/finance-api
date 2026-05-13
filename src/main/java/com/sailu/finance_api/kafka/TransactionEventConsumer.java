package com.sailu.finance_api.kafka;

import com.sailu.finance_api.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventConsumer {

    @KafkaListener(
            topics = "transaction-events",
            groupId = "finance-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload TransactionEvent event) {
        log.info("═══════════════════════════════════════════");
        log.info("Transaction Event Received");
        log.info("Reference  : {}", event.getReferenceNumber());
        log.info("Account    : {}", event.getAccountId());
        log.info("Type       : {}", event.getType());
        log.info("Amount     : {} INR", event.getAmount());
        log.info("Status     : {}", event.getStatus());
        log.info("Timestamp  : {}", event.getTimestamp());
        log.info("═══════════════════════════════════════════");
    }
}