package com.teambiund.bander.auth_server.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfo {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("kakao_account")
  private KakaoAccount kakaoAccount;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class KakaoAccount {
    @JsonProperty("email")
    private String email;

    @JsonProperty("email_needs_agreement")
    private Boolean emailNeedsAgreement;

    @JsonProperty("is_email_valid")
    private Boolean isEmailValid;

    @JsonProperty("is_email_verified")
    private Boolean isEmailVerified;
  }

  public String getEmail() {
    if (kakaoAccount != null && kakaoAccount.getEmail() != null) {
      return kakaoAccount.getEmail();
    }
    return null;
  }
}
