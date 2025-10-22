package com.teambiund.bander.auth_server.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private String deviceId;
}
