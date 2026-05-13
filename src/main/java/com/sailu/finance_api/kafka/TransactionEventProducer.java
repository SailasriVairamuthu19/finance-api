package com.sailu.finance_api.kafka;

import com.sailu.finance_api.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    private static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(TOPIC, event.getAccountId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction event published: {} | partition: {} | offset: {}",
                        event.getReferenceNumber(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish transaction event: {} | error: {}",
                        event.getReferenceNumber(), ex.getMessage());
            }
        });
    }
}