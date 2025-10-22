package com.teambiund.bander.auth_server.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginRequest {

  @NotBlank(message = "액세스 토큰은 필수입니다")
  private String accessToken;

  private String deviceId;
}
