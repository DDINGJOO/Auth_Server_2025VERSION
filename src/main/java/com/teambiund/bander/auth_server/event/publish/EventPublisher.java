package com.teambiund.bander.auth_server.event.publish;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void publish(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
}
