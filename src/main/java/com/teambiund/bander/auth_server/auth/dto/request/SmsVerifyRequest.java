package com.teambiund.bander.auth_server.auth.dto.request;

import com.teambiund.bander.auth_server.auth.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SmsVerifyRequest {
  @NotBlank(message = "사용자 ID는 필수 입력 항목입니다")
  private String userId;

  @NotBlank(message = "전화번호는 필수 입력 항목입니다")
  @PhoneNumber
  private String phoneNumber;

  @NotBlank(message = "인증 코드는 필수 입력 항목입니다")
  @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다")
  private String code;
}
