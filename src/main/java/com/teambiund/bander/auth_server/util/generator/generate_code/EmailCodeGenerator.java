package com.teambiund.bander.auth_server.util.generator.generate_code;


import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailCodeGenerator {
    private static final int DEFAULT_EXPIRE_SECONDS = 290; // Fallback to 6 minutes
    private final AuthRepository authRepository;
    private final StringRedisTemplate redisTemplate;
    @Value("${email.code.prefix}")
    private String CODE_PREFIX = "email:";
    @Value("${email.code.length}")
    private int CODE_LENGTH = 6;
    @Value("${email.code.expire.time}")
    private int CODE_EXPIRE_TIME = 290;

    // 6글자, TTL 6분
    public String generateCode(String email) {
        if (authRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

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
        if (code.equals("confirmed")) {
            String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
            if (expect == null) {
                return false;
            }
            if (!expect.equals("confirmed")) {
                throw new CustomException(ErrorCode.NOT_CONFIRMED_EMAIL);
            }
            redisTemplate.delete(CODE_PREFIX + email);
            return true;
        }
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (expect == null) {
            return false;
        }
        if (!expect.equals(code)) {
            return false;
        }
        // Delete the key associated with the email, not the code
        redisTemplate.delete(CODE_PREFIX + email);
        redisTemplate.opsForValue().set(CODE_PREFIX + email, "confirmed", 300);
        return true;
    }

    public boolean resendEmail(String email) {
        String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        return expect == null;
    }
}
