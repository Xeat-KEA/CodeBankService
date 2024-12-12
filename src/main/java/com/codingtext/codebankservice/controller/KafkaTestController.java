package com.codingtext.codebankservice.controller;

import com.codingtext.codebankservice.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaTestController {


    private final KafkaProducerService kafkaProducerService;

    @GetMapping("/sendMessage")
    public String sendMessage(@RequestParam String topic, @RequestParam String key, @RequestParam String message) {

        kafkaProducerService.sendMessage(topic, key, message);
        return "Message sent to Kafka topic: " + topic;
    }
}
