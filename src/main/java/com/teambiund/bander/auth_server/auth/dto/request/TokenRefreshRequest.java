package com.teambiund.bander.auth_server.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRefreshRequest {
  @NotBlank(message = "리프레시 토큰은 필수입니다")
  private String refreshToken;

  @NotBlank(message = "디바이스 ID는 필수입니다")
  private String deviceId;
}
