package com.teambiund.bander.auth_server.event.publish;

import com.teambiund.bander.auth_server.event.events.EmailConfirmRequest;
import com.teambiund.bander.auth_server.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConfirmRequestEventPub {
    private final RedisUtil redisUtil;
    private final EventPublisher eventPublisher;

    private final String TOPIC = "email-confirm-request";

    public void emailConfirmReq(String email) {
        String code = redisUtil.generateCode(email);
        EmailConfirmRequest req = EmailConfirmRequest.builder()
                .email(email)
                .code(code)
                .build();
        eventPublisher.publish(TOPIC, req);
    }
}
