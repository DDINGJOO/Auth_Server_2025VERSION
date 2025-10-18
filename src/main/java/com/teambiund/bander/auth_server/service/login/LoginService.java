package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.dto.response.LoginResponse;

public interface LoginService {
  LoginResponse login(String email, String password);

  LoginResponse refreshToken(String refreshToken, String deviceId);
}
