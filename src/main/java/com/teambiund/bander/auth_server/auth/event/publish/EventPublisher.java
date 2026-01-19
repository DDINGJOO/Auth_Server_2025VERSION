package com.teambiund.bander.auth_server.auth.event.publish;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void publish(String topic, Object message) {
    // JsonSerializer가 자동으로 직렬화하므로 객체를 직접 전송
    kafkaTemplate.send(topic, message);
  }
}
