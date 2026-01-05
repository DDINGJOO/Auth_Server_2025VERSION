package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.SocialLoginControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.SocialLoginRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.service.social.SocialLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
public class SocialLoginController implements SocialLoginControllerSwagger {

  private final SocialLoginService socialLoginService;

  @Override
  @PostMapping("/kakao")
  public ResponseEntity<LoginResponse> kakaoLogin(@Valid @RequestBody SocialLoginRequest request) {
    log.info("카카오 로그인 요청");
    LoginResponse response = socialLoginService.kakaoLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  @PostMapping("/apple")
  public ResponseEntity<LoginResponse> appleLogin(@Valid @RequestBody SocialLoginRequest request) {
    log.info("애플 로그인 요청");
    LoginResponse response = socialLoginService.appleLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  @PostMapping("/google")
  public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody SocialLoginRequest request) {
    log.info("구글 로그인 요청");
    LoginResponse response = socialLoginService.googleLogin(request.getAccessToken());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
