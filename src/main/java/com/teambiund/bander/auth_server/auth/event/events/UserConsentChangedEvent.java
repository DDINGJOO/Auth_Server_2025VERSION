package com.teambiund.bander.auth_server.auth.event.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserConsentChangedEvent {
  private String userId;
  private String consentId;
  private Boolean consented;
  private String changedAt;
}