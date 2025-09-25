package com.teambiund.bander.auth_server.util.generator.token.impl;

import com.teambiund.bander.auth_server.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class JWTTokenUtilTest {

    private JWTTokenUtil provider;

    @BeforeEach
    void setUp() throws Exception {
        provider = new JWTTokenUtil();
        // Inject secret via reflection
        Field f = JWTTokenUtil.class.getDeclaredField("jwtSecret");
        f.setAccessible(true);
        f.set(provider, "test-secret-123");
    }

    @Test
    @DisplayName("Generate access token and validate/extract claims")
    void generateAccessToken_andValidate() {
        String userId = "user-1";
        Role role = Role.USER;
        String deviceId = "dev1";

        String token = provider.generateAccessToken(userId, role, deviceId);
        assertThat(token).isNotNull();
        String[] parts = token.split("\\.");
        assertThat(parts.length).isEqualTo(3);

        assertThat(provider.isValid(token)).isTrue();
        assertThat(provider.extractUserId(token)).isEqualTo(userId);
        assertThat(provider.extractRole(token)).isEqualTo(role);
        assertThat(provider.extractDeviceId(token)).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("isValid should return false when signature is tampered")
    void invalidWhenSignatureTampered() {
        String token = provider.generateAccessToken("user-x", Role.ADMIN, "dv");
        String[] parts = token.split("\\.");
        String sig = parts[2];
        // Change last character to a different one
        char last = sig.charAt(sig.length() - 1);
        char replacement = last == 'A' ? 'B' : 'A';
        String tamperedSig = sig.substring(0, sig.length() - 1) + replacement;
        String tampered = parts[0] + "." + parts[1] + "." + tamperedSig;

        assertThat(provider.isValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Generate refresh token and validate")
    void generateRefreshToken_andValidate() {
        String token = provider.generateRefreshToken("user-2", Role.GUEST, "d2");
        assertThat(provider.isValid(token)).isTrue();
    }
}
