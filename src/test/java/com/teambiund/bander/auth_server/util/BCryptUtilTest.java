package com.teambiund.bander.auth_server.util;

import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptUtilTest {
    private final BCryptUtil passwordEncoder = new BCryptUtil();

    @Test
    @DisplayName("비밀번호 암/복호화 테스트")
    void hash() {
        String hashed = passwordEncoder.encode("hoss1001!");
        assertTrue(passwordEncoder.matches("hoss1001!", hashed));
    }

    @Test
    @DisplayName("비밀번호 암호화 테스트 : 같은 암호여도 다른해시")
    void sameHash(){
        String hashed = passwordEncoder.encode("hoss1001!");
        String sameHash = passwordEncoder.encode("hoss1001!");
        assertNotEquals(hashed, sameHash);
    }

    @Test
    @DisplayName("비밀번호 암호화 테스트 : 같은 암호 -> 다른 해시 -> 각 복호화 성공")
    void sameHashVerify(){
        String hashed = passwordEncoder.encode("hoss1001!");
        String sameHash = passwordEncoder.encode("hoss1001!");
        assertNotEquals(hashed, sameHash);
        assertTrue(passwordEncoder.matches("hoss1001!", hashed));
        assertTrue(passwordEncoder.matches("hoss1001!", sameHash));
    }
}
