package com.teambiund.bander.auth_server.event.publish;

import com.teambiund.bander.auth_server.event.events.EmailConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConfirmRequestEventPub {
    private final EventPublisher eventPublisher;

    private final String TOPIC = "email-confirm-request";

    public void emailConfirmReq(EmailConfirmRequest req) {
        eventPublisher.publish(TOPIC, req);
    }
}
