package com.teambiund.bander.auth_server.auth.event.publish;

import com.teambiund.bander.auth_server.auth.event.events.PhoneNumberVerifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneNumberVerifiedEventPub {
  private final EventPublisher eventPublisher;

  private static final String TOPIC = "phone-number-verified";

  public void publish(PhoneNumberVerifiedEvent event) {
    eventPublisher.publish(TOPIC, event);
  }
}