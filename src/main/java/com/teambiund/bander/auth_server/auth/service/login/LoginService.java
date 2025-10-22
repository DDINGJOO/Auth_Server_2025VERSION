package com.teambiund.bander.auth_server.auth.service.login;

import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;

public interface LoginService {
  LoginResponse login(String email, String password);

  LoginResponse refreshToken(String refreshToken, String deviceId);

  LoginResponse generateLoginResponse(Auth auth);
}
