package com.teambiund.bander.auth_server.auth.util.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, String> redisTemplate;

    private final String CODE_PREFIX = "user:";
    private final int CODE_LENGTH = 6;
    private final int CODE_EXPIRE_TIME = 60 * 5;


    public String generateCode(String email) {
        StringBuilder code = new StringBuilder();
        while (code.length() < CODE_LENGTH) {
            code.append((int) (Math.random() * 10));
        }
        redisTemplate.opsForValue().set(CODE_PREFIX + code.toString(), email, Duration.ofSeconds(CODE_EXPIRE_TIME));
        return code.toString();
    }


    public String checkCode(String code) {
        String email = redisTemplate.opsForValue().get(CODE_PREFIX + code);
        if (email == null) {
            return null;
        }
        redisTemplate.delete(CODE_PREFIX + code);
        return email;
    }
}
