package com.teambiund.bander.auth_server.auth.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambiund.bander.auth_server.auth.dto.response.GoogleUserInfo;
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
public class GoogleOAuthClient {

  private final ObjectMapper objectMapper;

  public GoogleUserInfo getUserInfo(String idToken) {
    try {
      String[] tokenParts = idToken.split("\\.");
      if (tokenParts.length != 3) {
        log.error("Google ID Token 형식 오류");
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

      if (email == null || sub == null) {
        log.error("Google 사용자 정보 조회 실패: 이메일 또는 sub 정보 없음");
        throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
      }

      log.info("Google 사용자 정보 조회 성공: {}", email);

      return GoogleUserInfo.builder().sub(sub).email(email).emailVerified(emailVerified).build();

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("Google ID Token 파싱 중 오류 발생", e);
      throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
    }
  }
}
