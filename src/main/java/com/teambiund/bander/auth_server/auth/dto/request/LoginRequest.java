package com.teambiund.bander.auth_server.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank(message = "이메일은 필수 입력 항목입니다")
  @Email(message = "올바른 이메일 형식이 아닙니다", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
  private String password;
}
