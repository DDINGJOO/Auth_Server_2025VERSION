package com.teambiund.bander.auth_server.auth.util.generator.generate_code;

import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsCodeGenerator {
  private static final int DEFAULT_EXPIRE_SECONDS = 300; // 5분
  private static final String CODE_PREFIX = "sms:";

  private final StringRedisTemplate redisTemplate;

  @Value("${sms.code.length:6}")
  private int codeLength;

  @Value("${sms.code.expire.time:300}")
  private int codeExpireTime;

  public SmsCodeGenerator(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * SMS 인증 코드 생성 및 Redis 저장
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @return 생성된 인증 코드
   */
  public String generateCode(String userId, String phoneNumber) {
    String key = buildKey(userId, phoneNumber);

    // 이미 발급된 코드가 있는지 확인
    if (redisTemplate.opsForValue().get(key) != null) {
      throw new CustomException(AuthErrorCode.ALREADY_GENERATE_CODE);
    }

    // 6자리 인증 코드 생성
    StringBuilder code = new StringBuilder();
    while (code.length() < codeLength) {
      code.append((int) (Math.random() * 10));
    }

    String result = code.toString();

    // Redis에 저장 (TTL 적용)
    int ttl = codeExpireTime > 0 ? codeExpireTime : DEFAULT_EXPIRE_SECONDS;
    Duration expireSeconds = Duration.ofSeconds(ttl);
    redisTemplate.opsForValue().set(key, result, expireSeconds);

    log.info("SMS 인증 코드 Redis 저장 완료 - key: {}, userId: {}", key, userId);
    return result;
  }

  /**
   * SMS 인증 코드 검증
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @param code 인증 코드
   * @return 인증 성공 여부
   */
  public boolean checkCode(String userId, String phoneNumber, String code) {
    String key = buildKey(userId, phoneNumber);
    String storedCode = redisTemplate.opsForValue().get(key);

    if (storedCode == null) {
      log.info("SMS 인증 코드가 존재하지 않음 - userId: {}", userId);
      return false;
    }

    if (!storedCode.equals(code)) {
      log.info("SMS 인증 코드 불일치 - userId: {}", userId);
      return false;
    }

    // 인증 성공 시 코드를 "confirmed"로 변경
    redisTemplate.opsForValue().set(key, "confirmed", Duration.ofMinutes(10));
    log.info("SMS 인증 성공 - userId: {}", userId);
    return true;
  }

  /**
   * 인증 완료 여부 확인 및 키 삭제
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @return 인증 완료 여부
   */
  public boolean isConfirmed(String userId, String phoneNumber) {
    String key = buildKey(userId, phoneNumber);
    String storedValue = redisTemplate.opsForValue().get(key);

    if (storedValue == null || !storedValue.equals("confirmed")) {
      return false;
    }

    // 확인 후 키 삭제
    redisTemplate.delete(key);
    return true;
  }

  /**
   * 재발신 가능 여부 확인
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   * @return 재발신 가능 여부
   */
  public boolean canResend(String userId, String phoneNumber) {
    String key = buildKey(userId, phoneNumber);
    return redisTemplate.opsForValue().get(key) == null;
  }

  /**
   * 기존 코드 삭제 (재발신을 위해)
   *
   * @param userId 사용자 ID
   * @param phoneNumber 전화번호
   */
  public void deleteCode(String userId, String phoneNumber) {
    String key = buildKey(userId, phoneNumber);
    redisTemplate.delete(key);
  }

  private String buildKey(String userId, String phoneNumber) {
    return CODE_PREFIX + userId + ":" + phoneNumber;
  }
}