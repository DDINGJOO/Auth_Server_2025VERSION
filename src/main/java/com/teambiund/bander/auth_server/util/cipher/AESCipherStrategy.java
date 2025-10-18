package com.teambiund.bander.auth_server.util.cipher;

import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256 대칭키 암호화 전략
 * - 복호화 가능 (isReversible = true)
 * - 전화번호, 이메일 등 복호화가 필요한 개인정보에 사용
 *
 * 특징:
 * - AES/ECB/PKCS5Padding 알고리즘
 * - SHA-256으로 키 파생
 * - Base64 인코딩
 *
 * 사용 예:
 * - 전화번호 암호화/복호화
 * - 이메일 암호화/복호화
 * - 주소 암호화/복호화
 */
public class AESCipherStrategy implements CipherStrategy {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private final SecretKeySpec secretKey;

    /**
     * 생성자
     * @param encryptionKey 암호화 키 (환경변수에서 주입)
     */
    public AESCipherStrategy(String encryptionKey) {
        this.secretKey = generateKey(encryptionKey);
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ENCRYPTION_ERROR);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DECRYPTION_ERROR);
        }
    }

    @Override
    public boolean matches(String plainText, String encryptedText) {
        // AES는 대칭키이므로 복호화 후 비교
        String decrypted = decrypt(encryptedText);
        return plainText != null && plainText.equals(decrypted);
    }

    @Override
    public boolean isReversible() {
        return true; // AES는 복호화 가능
    }

    /**
     * 문자열 키를 AES-256 키로 변환
     * @param key 원본 키 문자열
     * @return SecretKeySpec (AES-256)
     */
    private SecretKeySpec generateKey(String key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            keyBytes = Arrays.copyOf(keyBytes, 32); // AES-256은 32바이트
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
}
