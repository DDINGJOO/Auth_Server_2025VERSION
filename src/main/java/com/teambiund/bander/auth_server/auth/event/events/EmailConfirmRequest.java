package com.teambiund.bander.auth_server.auth.event.events;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfirmRequest {
  private String email;
  private String code;
}
