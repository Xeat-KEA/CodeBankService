package com.codingtext.codebankservice.kafka;

import com.codingtext.codebankservice.Dto.User.UserPoint;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserPointProducer {

    private final KafkaTemplate<String, UserPoint> kafkaTemplate;

    public UserPointProducer(KafkaTemplate<String, UserPoint> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserPoint(String topic, UserPoint userPoint) {
        kafkaTemplate.send(topic, userPoint); // UserPoint 객체 발행
        System.out.println("Message sent to Kafka topic: " + topic + " with data: " + userPoint);
    }
}
