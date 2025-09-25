package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.util.generator.token.impl.JWTTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenStoreService {
    private final String TOKEN_PREFIX = "accessToken:";
    private RedisTemplate<String, String> redisTemplate;
    @Value("${security.jwt.access-token-expire-time}")
    private Long accessTokenTTL;
    private JWTTokenUtil jwtTokenUtil;

    public void TokenStored(String accessToken) {
        //accessToken:Token
        String userId = jwtTokenUtil.extractUserId(accessToken);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + accessToken, userId, Duration.ofSeconds(accessTokenTTL + 30));
    }
}
