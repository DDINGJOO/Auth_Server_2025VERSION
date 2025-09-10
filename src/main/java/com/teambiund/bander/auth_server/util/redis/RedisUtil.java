package com.teambiund.bander.auth_server.util.redis;

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

    public String generateCode(String userId) {
        StringBuilder code = new StringBuilder();


        while (code.length() < CODE_LENGTH) {
            code.append((int) (Math.random() * 10));
        }


        redisTemplate.opsForValue().set(CODE_PREFIX + userId, code.toString(), CODE_EXPIRE_TIME);
        return code.toString();
    }

    public boolean checkCode(String email, String code) {
        String redisCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (redisCode == null) {
            return false;
        }
        return redisCode.equals(code) ? redisTemplate.delete(CODE_PREFIX + email) : false;
    }
}
