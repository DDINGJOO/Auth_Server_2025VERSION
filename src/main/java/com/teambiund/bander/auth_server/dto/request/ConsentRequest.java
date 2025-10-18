package com.teambiund.bander.auth_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentRequest {
  @NotBlank(message = "동의 항목 이름은 필수입니다")
  private String consentName;

  @NotBlank(message = "동의 항목 버전은 필수입니다")
  private String version;

  private boolean consented;
}
