package com.teambiund.bander.auth_server.util.generator.token_generator.impl;

import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.util.generator.token_generator.TokenProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JWTTokenProvider implements TokenProvider {

    @Override
    public String generateAccessToken(String userId, Role role) {
        return "";
    }

    @Override
    public String generateRefreshToken(String userId, Role role) {
        return "";
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString().substring(0, 4);

    }
}
