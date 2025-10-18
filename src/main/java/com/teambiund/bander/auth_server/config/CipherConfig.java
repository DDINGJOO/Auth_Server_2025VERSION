package com.teambiund.bander.auth_server.config;

import com.teambiund.bander.auth_server.util.cipher.AESCipherStrategy;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.util.cipher.PBKDF2CipherStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 암호화 전략 설정 (Strategy Pattern) - 모든 암호화 전략을 중앙에서 관리 - CipherStrategy 기반 통합 설계
 *
 * <p>전략 목록: 1. AESCipherStrategy: 대칭키 암호화 (전화번호, 이메일 등) 2. PBKDF2CipherStrategy: 단방향 해시 (비밀번호 등)
 *
 * <p>보안 권장사항: 1. application.yml 또는 환경변수에 암호화 키 설정 2. 프로덕션 환경에서는 KMS(Key Management Service) 사용 권장
 * 3. 암호화 키는 절대 코드에 하드코딩하지 말 것
 */
@Configuration
public class CipherConfig {

  /** AESCipherStrategy 빈 등록 - 도메인 중립적인 AES 암호화 전략 - 전화번호, 이메일 등 다른 개인정보 암호화에 사용 */
  @Bean("aesCipherStrategy")
  public CipherStrategy aesCipherStrategy(
      @Value("${security.aes.encryption-key:default-aes-encryption-key-change-in-production}")
          String encryptionKey) {
    return new AESCipherStrategy(encryptionKey);
  }

  /**
   * PBKDF2CipherStrategy 빈 등록 - PBKDF2WithHmacSHA256 단방향 해시 전략 - 비밀번호, API 키 등 보안 데이터 해싱에 사용 -
   * BCrypt 대안으로 더 빠르고 안정적
   */
  @Bean("pbkdf2CipherStrategy")
  public CipherStrategy pbkdf2CipherStrategy() {
    return new PBKDF2CipherStrategy();
  }
}
