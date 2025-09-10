package com.teambiund.bander.auth_server.util.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, String> redisTemplate;

    private final String CODE_PREFIX = "user:";
    private final int CODE_LENGTH = 6;
    private final int CODE_EXPIRE_TIME = 60 * 5;

    public String generateCode(String userId) {
        StringBuilder code = new StringBuilder();
        while (code.length() < CODE_LENGTH) {
            code.append((int) (Math.random() * 10));
        }
        redisTemplate.opsForValue().set(CODE_PREFIX + code.toString(), userId, Duration.ofSeconds(CODE_EXPIRE_TIME));
        return code.toString();
    }


    public String checkCode(String code) {
        String userId = redisTemplate.opsForValue().get(CODE_PREFIX + code);
        if (userId == null) {
            return null;
        }
        redisTemplate.delete(CODE_PREFIX + code);
        return userId;
    }
}
