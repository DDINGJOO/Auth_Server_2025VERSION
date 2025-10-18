package com.teambiund.bander.auth_server.util.cipher;

import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2WithHmacSHA256 단방향 해시 전략
 * - BCrypt의 대안으로 사용
 * - 복호화 불가능 (isReversible = false)
 * - 비밀번호 등 복호화가 불필요한 보안 데이터에 사용
 *
 * 특징:
 * - PBKDF2WithHmacSHA256 알고리즘 사용
 * - Salt 자동 생성 (32바이트)
 * - 반복 횟수: 65,536회 (OWASP 권장)
 * - 해시 길이: 256비트 (32바이트)
 *
 * 사용 예:
 * - 비밀번호 저장/검증
 * - API 키 저장/검증
 */
public class PBKDF2CipherStrategy implements CipherStrategy {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536; // OWASP 권장 최소값
    private static final int SALT_LENGTH = 32; // 32 bytes (256 bits)
    private static final int HASH_LENGTH = 256; // 256 bits

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            // Salt 생성
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // 해시 생성
            byte[] hash = pbkdf2(plainText.toCharArray(), salt, ITERATIONS, HASH_LENGTH);

            // Salt와 Hash를 결합하여 Base64 인코딩
            // 형식: [iterations]:[salt]:[hash]
            return ITERATIONS + ":" +
                    Base64.getEncoder().encodeToString(salt) + ":" +
                    Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ENCRYPTION_ERROR);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        throw new UnsupportedOperationException(
                "PBKDF2 is a one-way hash function. Decryption is not supported. Use matches() instead."
        );
    }

    @Override
    public boolean matches(String plainText, String encryptedText) {
        if (plainText == null || encryptedText == null) {
            return false;
        }

        try {
            // 저장된 해시 파싱: [iterations]:[salt]:[hash]
            String[] parts = encryptedText.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);

            // 입력된 비밀번호로 해시 생성
            byte[] testHash = pbkdf2(plainText.toCharArray(), salt, iterations, hash.length * 8);

            // 상수 시간 비교 (timing attack 방지)
            return slowEquals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isReversible() {
        return false; // PBKDF2는 복호화 불가능
    }

    /**
     * PBKDF2 해시 생성
     */
    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * 상수 시간 비교 (timing attack 방지)
     * - 모든 바이트를 비교하여 실행 시간이 일정하도록 함
     */
    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
