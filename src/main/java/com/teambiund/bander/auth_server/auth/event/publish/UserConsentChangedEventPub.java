package com.teambiund.bander.auth_server.auth.event.publish;

import com.teambiund.bander.auth_server.auth.event.events.UserConsentChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConsentChangedEventPub {
  private final EventPublisher eventPublisher;

  private static final String TOPIC = "user-consent-changed";

  public void publish(UserConsentChangedEvent event) {
    eventPublisher.publish(TOPIC, event);
  }
}