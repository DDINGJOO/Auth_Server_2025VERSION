package com.teambiund.bander.auth_server.auth.dto.request;

import com.teambiund.bander.auth_server.auth.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsCodeRequest {
  @NotBlank(message = "사용자 ID는 필수 입력 항목입니다")
  private String userId;

  @NotBlank(message = "전화번호는 필수 입력 항목입니다")
  @PhoneNumber
  private String phoneNumber;
}
