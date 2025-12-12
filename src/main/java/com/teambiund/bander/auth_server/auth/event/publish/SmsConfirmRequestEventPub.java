package com.teambiund.bander.auth_server.auth.event.publish;

import com.teambiund.bander.auth_server.auth.event.events.SmsConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsConfirmRequestEventPub {
  private final EventPublisher eventPublisher;

  private static final String TOPIC = "sms-confirm-request";

  public void publish(SmsConfirmRequest req) {
    eventPublisher.publish(TOPIC, req);
  }
}
