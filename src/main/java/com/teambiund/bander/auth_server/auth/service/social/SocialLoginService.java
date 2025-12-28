package com.teambiund.bander.auth_server.auth.service.social;

import com.teambiund.bander.auth_server.auth.dto.response.AppleUserInfo;
import com.teambiund.bander.auth_server.auth.dto.response.GoogleUserInfo;
import com.teambiund.bander.auth_server.auth.dto.response.KakaoUserInfo;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.oauth.AppleOAuthClient;
import com.teambiund.bander.auth_server.auth.oauth.GoogleOAuthClient;
import com.teambiund.bander.auth_server.auth.oauth.KakaoOAuthClient;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.login.LoginService;
import com.teambiund.bander.auth_server.auth.service.signup.SignupService;
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
  private final GoogleOAuthClient googleOAuthClient;
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

  public LoginResponse googleLogin(String idToken) {
    GoogleUserInfo userInfo = googleOAuthClient.getUserInfo(idToken);
    String email = userInfo.getEmail();

    return processLogin(email, Provider.GOOGLE);
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
        throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
      }
      log.info("기존 사용자 로그인 처리: email={}, provider={}", email, provider);
    }

    return loginService.generateLoginResponse(auth);
  }
}
