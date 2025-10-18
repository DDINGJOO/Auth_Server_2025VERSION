package com.teambiund.bander.auth_server.event.publish;

import com.teambiund.bander.auth_server.event.events.CreateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProfileRequestEventPub {
  private final EventPublisher eventPublisher;

  private final String TOPIC = "profile-create-request";

  public void createProfileRequestPub(CreateProfileRequest req) {
    eventPublisher.publish(TOPIC, req);
  }
}
