package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.LoginControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.LoginRequest;
import com.teambiund.bander.auth_server.auth.dto.request.TokenRefreshRequest;
import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.enums.AppType;
import com.teambiund.bander.auth_server.auth.service.login.LoginService;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/login")
@RequiredArgsConstructor
public class LoginController implements LoginControllerSwagger {

  private final LoginService loginService;

  @Override
  @PostMapping("")
  public LoginResponse login(
      @Valid @RequestBody LoginRequest loginRequest,
      @RequestHeader(value = "X-App-Type", required = false) String appTypeHeader) {
    AppType appType =
        Optional.ofNullable(appTypeHeader)
            .filter(s -> !s.isBlank())
            .map(AppType::valueOf)
            .orElse(AppType.GENERAL);
    return loginService.login(loginRequest.getEmail(), loginRequest.getPassword(), appType);
  }

  @Override
  @PostMapping("/refreshToken")
  public LoginResponse refreshToken(
      @Valid @RequestBody TokenRefreshRequest request,
      @RequestHeader(value = "X-App-Type", required = false) String appTypeHeader) {
    AppType appType =
        Optional.ofNullable(appTypeHeader)
            .filter(s -> !s.isBlank())
            .map(AppType::valueOf)
            .orElse(AppType.GENERAL);
    return loginService.refreshToken(request.getRefreshToken(), request.getDeviceId(), appType);
  }
}
