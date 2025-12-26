package com.teambiund.bander.auth_server.auth.service.login;

import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.AppType;

public interface LoginService {
  LoginResponse login(String email, String password, AppType appType);

  LoginResponse refreshToken(String refreshToken, String deviceId, AppType appType);

  LoginResponse generateLoginResponse(Auth auth);
}
