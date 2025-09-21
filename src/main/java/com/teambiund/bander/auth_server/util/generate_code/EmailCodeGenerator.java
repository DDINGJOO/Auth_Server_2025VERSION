package com.teambiund.bander.auth_server.util.generate_code;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailCodeGenerator {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${email.code.prefix}")
    private String CODE_PREFIX;
    @Value("${email.code.length}")
    private int CODE_LENGTH;

    @Value("${email.code.expire.time}")
    private int CODE_EXPIRE_TIME;

    // 6글자, TTL 6분
    public String generateCode(String email) {
        StringBuilder code = new StringBuilder();
        while (code.length() < CODE_LENGTH) {
            code.append((int) (Math.random() * 10));
        }
        String result = code.toString();
        redisTemplate.opsForValue().set(CODE_PREFIX + email, result, Duration.ofSeconds(CODE_EXPIRE_TIME));
        return code.toString();
    }

    public boolean checkCode(String code, String email) {
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (expect == null) {
            return false;
        }
        if (!expect.equals(code)) {
            return false;
        }
        redisTemplate.delete(CODE_PREFIX + code);
        return true;
    }

    public boolean resendEmail(String email) {
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        return expect == null;
    }
}
