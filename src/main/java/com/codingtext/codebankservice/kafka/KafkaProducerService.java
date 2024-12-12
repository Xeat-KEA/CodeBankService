package com.codingtext.codebankservice.kafka;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, String message) {
        // Kafka 메시지 전송
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);

        // Callback 추가
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Message sent successfully: " + result.getRecordMetadata());
            } else {
                System.err.println("Message failed to send: " + ex.getMessage());
            }
        });
    }
}
