package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.entity.Auth;

public interface LoginService {
  LoginResponse login(String email, String password);

  LoginResponse refreshToken(String refreshToken, String deviceId);

  LoginResponse generateLoginResponse(Auth auth, String deviceId);
}
