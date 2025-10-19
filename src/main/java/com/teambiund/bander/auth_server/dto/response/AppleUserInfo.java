package com.teambiund.bander.auth_server.dto.response;

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
public class AppleUserInfo {

  private String sub;
  private String email;
  private Boolean emailVerified;
  private Boolean isPrivateEmail;
}
