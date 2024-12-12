package com.codingtext.codebankservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, String message) {
        // Kafka 메시지 전송
        ListenableFuture<SendResult<String, String>> future = (ListenableFuture<SendResult<String, String>>) kafkaTemplate.send(topic, key, message);

        // Callback 추가
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Message sent successfully: " +
                        result.getRecordMetadata().toString());
            }

            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Message failed to send: " + ex.getMessage());
            }
        });
    }
}

