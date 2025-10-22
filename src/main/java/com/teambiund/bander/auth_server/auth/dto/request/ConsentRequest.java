package com.teambiund.bander.auth_server.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequest {
  @NotBlank(message = "동의 항목 ID는 필수입니다")
  private String consentId;

  private boolean consented;
}
