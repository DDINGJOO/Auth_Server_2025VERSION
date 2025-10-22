package com.teambiund.bander.auth_server.auth.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspendRequest {
  @NotBlank(message = "정지 사유는 필수입니다")
  private String suspendReason;

  @NotBlank(message = "정지 처리자 ID는 필수입니다")
  private String suspenderUserId;

  @NotBlank(message = "정지 대상자 ID는 필수입니다")
  private String suspendedUserId;

  @NotNull(message = "정지 일수는 필수입니다")
  @Min(value = 1, message = "정지 일수는 최소 1일 이상이어야 합니다")
  private long suspendDay;
}
