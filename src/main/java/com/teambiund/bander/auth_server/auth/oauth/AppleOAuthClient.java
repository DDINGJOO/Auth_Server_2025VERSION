package com.teambiund.bander.auth_server.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambiund.bander.auth_server.auth.dto.response.AppleUserInfo;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuthClient {

  private final ObjectMapper objectMapper;

  public AppleUserInfo getUserInfo(String identityToken) {
    try {
      String[] tokenParts = identityToken.split("\\.");
      if (tokenParts.length != 3) {
        log.error("Apple Identity Token 형식 오류");
        throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
      }

      String payload = tokenParts[1];
      String decodedPayload =
          new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);

      Map<String, Object> claims = objectMapper.readValue(decodedPayload, Map.class);

      String email = (String) claims.get("email");
      String sub = (String) claims.get("sub");
      Boolean emailVerified =
          claims.get("email_verified") != null
              ? Boolean.valueOf(claims.get("email_verified").toString())
              : false;
      Boolean isPrivateEmail =
          claims.get("is_private_email") != null
              ? Boolean.valueOf(claims.get("is_private_email").toString())
              : false;

      if (email == null || sub == null) {
        log.error("Apple 사용자 정보 조회 실패: 이메일 또는 sub 정보 없음");
        throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
      }

      log.info("Apple 사용자 정보 조회 성공: {}", email);

      return AppleUserInfo.builder()
          .sub(sub)
          .email(email)
          .emailVerified(emailVerified)
          .isPrivateEmail(isPrivateEmail)
          .build();

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("Apple Identity Token 파싱 중 오류 발생", e);
      throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
    }
  }
}
