package com.teambiund.bander.auth_server.util.generator.token_generator;

import com.teambiund.bander.auth_server.enums.Role;

public interface TokenProvider {
    String generateAccessToken(String userId, Role role);

    String generateRefreshToken(String userId, Role role);
}
