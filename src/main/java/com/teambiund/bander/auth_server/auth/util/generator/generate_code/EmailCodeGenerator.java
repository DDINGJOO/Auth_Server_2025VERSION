package com.teambiund.bander.auth_server.auth.util.generator.generate_code;

import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailCodeGenerator {
  private static final int DEFAULT_EXPIRE_SECONDS = 290; // Fallback to 6 minutes
  private final AuthRepository authRepository;
  private final StringRedisTemplate redisTemplate;
  private final CipherStrategy emailCipher;

  @Value("${email.code.prefix}")
  private String CODE_PREFIX = "email:";

  @Value("${email.code.length}")
  private int CODE_LENGTH = 6;

  @Value("${email.code.expire.time}")
  private int CODE_EXPIRE_TIME = 290;

  public EmailCodeGenerator(
      AuthRepository authRepository,
      StringRedisTemplate redisTemplate,
      @Qualifier("aesCipherStrategy") CipherStrategy emailCipher) {
    this.authRepository = authRepository;
    this.redisTemplate = redisTemplate;
    this.emailCipher = emailCipher;
  }

  // 6글자, TTL 6분
  public String generateCode(String email) {
    String encryptedEmail = emailCipher.encrypt(email);
    if (authRepository.existsByEmail(encryptedEmail) || authRepository.existsByEmail(email)) {
      throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
    }

    if (redisTemplate.opsForValue().get(CODE_PREFIX + email) != null) {
      throw new CustomException(AuthErrorCode.ALREADY_GENERATE_CODE);
    }

    StringBuilder code = new StringBuilder();
    while (code.length() < CODE_LENGTH) {
      code.append((int) (Math.random() * 10));
    }

    String result = code.toString();

    int ttl = CODE_EXPIRE_TIME > 0 ? CODE_EXPIRE_TIME : DEFAULT_EXPIRE_SECONDS;
    Duration expireSeconds = Duration.ofSeconds(ttl);
    redisTemplate.opsForValue().set(CODE_PREFIX + email, result, expireSeconds);
	log.info("이메일 확인 코드 레디스 저장 완료 , key : {}, value : {}", CODE_PREFIX + email, result);
    return result;
  }

  public boolean checkCode(String code, String email) {
    if (code.equals("confirmed")) {
      String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
      if (expect == null) {
		  log.info("이메일이 확인되지 않은 유저입니다 : {}", email);
        throw new CustomException(AuthErrorCode.NOT_CONFIRMED_EMAIL);
      }
      if (!expect.equals("confirmed")) {
	      log.info("이메일 확인 작업을 하지 않은 유저입니다 : {}", email);
        throw new CustomException(AuthErrorCode.NOT_CONFIRMED_EMAIL);
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
    redisTemplate.opsForValue().getAndSet(CODE_PREFIX + email, "confirmed");
    return true;
  }

  public boolean resendEmail(String email) {
    String expect = redisTemplate.opsForValue().get(CODE_PREFIX + email);
    return expect == null;
  }
}
