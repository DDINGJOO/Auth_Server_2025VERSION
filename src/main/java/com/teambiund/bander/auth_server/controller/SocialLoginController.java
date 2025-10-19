package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.SocialLoginRequest;
import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.service.social.SocialLoginService;
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
@RequestMapping("/api/auth/social")
@RequiredArgsConstructor
public class SocialLoginController {

  private final SocialLoginService socialLoginService;

  @PostMapping("/kakao")
  public ResponseEntity<LoginResponse> kakaoLogin(@Valid @RequestBody SocialLoginRequest request) {
    log.info("카카오 로그인 요청");
    LoginResponse response =
        socialLoginService.kakaoLogin(request.getAccessToken(), request.getDeviceId());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/apple")
  public ResponseEntity<LoginResponse> appleLogin(@Valid @RequestBody SocialLoginRequest request) {
    log.info("애플 로그인 요청");
    LoginResponse response =
        socialLoginService.appleLogin(request.getAccessToken(), request.getDeviceId());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
