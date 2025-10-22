package com.teambiund.bander.auth_server.auth.oauth;

import com.teambiund.bander.auth_server.auth.dto.response.KakaoUserInfo;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

  private final RestTemplate restTemplate;

  @Value("${oauth.kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}")
  private String userInfoUri;

  public KakaoUserInfo getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + accessToken);
    headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

    HttpEntity<String> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<KakaoUserInfo> response =
          restTemplate.exchange(userInfoUri, HttpMethod.GET, request, KakaoUserInfo.class);

      KakaoUserInfo userInfo = response.getBody();
      if (userInfo == null || userInfo.getEmail() == null) {
        log.error("카카오 사용자 정보 조회 실패: 이메일 정보 없음");
        throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
      }

      log.info("카카오 사용자 정보 조회 성공: {}", userInfo.getEmail());
      return userInfo;

    } catch (HttpClientErrorException e) {
      log.error("카카오 API 호출 실패: {}", e.getMessage());
      throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
    } catch (Exception e) {
      log.error("카카오 사용자 정보 조회 중 오류 발생", e);
      throw new CustomException(AuthErrorCode.SOCIAL_LOGIN_FAILED);
    }
  }
}
