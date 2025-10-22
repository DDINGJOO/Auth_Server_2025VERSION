package com.teambiund.bander.auth_server.auth.event.publish;

import com.teambiund.bander.auth_server.auth.event.events.CreatedUserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProfileRequestEventPub {
  private final EventPublisher eventPublisher;

  private final String TOPIC = "user-created";

  public void createProfileRequestPub(CreatedUserEvent req) {
    eventPublisher.publish(TOPIC, req);
  }
}
