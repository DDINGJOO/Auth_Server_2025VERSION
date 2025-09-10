package com.teambiund.bander.auth_server.event.publish;

import com.teambiund.bander.auth_server.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConfirmRequestEventPub {
    private final RedisUtil redisUtil;

    public void emailConfirmReq(String email) {
        String code = redisUtil.generateCode(email);


    }
}
