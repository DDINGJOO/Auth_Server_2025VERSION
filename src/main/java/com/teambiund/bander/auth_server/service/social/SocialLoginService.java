package com.teambiund.bander.auth_server.service.social;

import com.teambiund.bander.auth_server.client.oauth.AppleOAuthClient;
import com.teambiund.bander.auth_server.client.oauth.KakaoOAuthClient;
import com.teambiund.bander.auth_server.dto.response.AppleUserInfo;
import com.teambiund.bander.auth_server.dto.response.KakaoUserInfo;
import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.login.LoginService;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginService {

  private final KakaoOAuthClient kakaoOAuthClient;
  private final AppleOAuthClient appleOAuthClient;
  private final AuthRepository authRepository;
  private final SignupService signupService;
  private final LoginService loginService;

  public LoginResponse kakaoLogin(String accessToken) {
    KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(accessToken);
    String email = userInfo.getEmail();

    return processLogin(email, Provider.KAKAO);
  }

  public LoginResponse appleLogin(String identityToken) {
    AppleUserInfo userInfo = appleOAuthClient.getUserInfo(identityToken);
    String email = userInfo.getEmail();

    return processLogin(email, Provider.APPLE);
  }

  private LoginResponse processLogin(String email, Provider provider) {
    Auth auth = authRepository.findByEmail(email).orElse(null);

    if (auth == null) {
      log.info("신규 사용자 회원가입 처리: email={}, provider={}", email, provider);
      auth = signupService.signupFromOtherProvider(email, provider);
    } else {
      if (!auth.getProvider().equals(provider)) {
        log.error(
            "이미 다른 방식으로 가입된 이메일: email={}, existingProvider={}, requestProvider={}",
            email,
            auth.getProvider(),
            provider);
        throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
      }
      log.info("기존 사용자 로그인 처리: email={}, provider={}", email, provider);
    }

    return loginService.generateLoginResponse(auth);
  }
}
