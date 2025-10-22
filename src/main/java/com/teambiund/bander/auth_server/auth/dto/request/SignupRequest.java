package com.teambiund.bander.auth_server.auth.dto.request;

import com.teambiund.bander.auth_server.auth.validation.PasswordMatches;
import com.teambiund.bander.auth_server.auth.validation.RequiredConsents;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@PasswordMatches
@Builder
@AllArgsConstructor
public class SignupRequest {

  @NotBlank(message = "이메일은 필수 입력 항목입니다")
  @Email(message = "올바른 이메일 형식이 아닙니다", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력 항목입니다")
  @Pattern(
      regexp =
          "^(?=.{8,}$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]).+$",
      message = "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다")
  private String password;

  @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다")
  private String passwordConfirm;

  @NotNull(message = "동의 항목은 필수입니다")
  @Valid
  @RequiredConsents
  private List<ConsentRequest> consentReqs;
}
