package com.teambiund.bander.auth_server.auth.event.events;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsConfirmRequest {
  private String phoneNumber;
  private String code;
}
