package com.teambiund.bander.auth_server.util.generate_code;


import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailCodeGenerator {

    private static final int DEFAULT_EXPIRE_SECONDS = 360; // Fallback to 6 minutes
    private final StringRedisTemplate redisTemplate;
    @Value("${email.code.prefix}")
    private String CODE_PREFIX = "email:";
    @Value("${email.code.length}")
    private int CODE_LENGTH = 6;
    @Value("${email.code.expire.time}")
    private int CODE_EXPIRE_TIME = 360;

    // 6글자, TTL 6분
    public String generateCode(String email) {
        if (redisTemplate.opsForValue().get(CODE_PREFIX + email) != null) {
            throw new CustomException(ErrorCode.ALREADY_GENERATE_CODE);
        }
        StringBuilder code = new StringBuilder();
        while (code.length() < CODE_LENGTH) {
            code.append((int) (Math.random() * 10));
        }
        String result = code.toString();

        int ttl = CODE_EXPIRE_TIME > 0 ? CODE_EXPIRE_TIME : DEFAULT_EXPIRE_SECONDS;
        Duration expireSeconds = Duration.ofSeconds(ttl);
        redisTemplate.opsForValue().set(CODE_PREFIX + email, result, expireSeconds);
        return result;
    }

    public boolean checkCode(String code, String email) {
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (expect == null) {
            return false;
        }
        if (!expect.equals(code)) {
            return false;
        }
        // Delete the key associated with the email, not the code
        redisTemplate.delete(CODE_PREFIX + email);
        return true;
    }

    public boolean resendEmail(String email) {
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        return expect == null;
    }
}
