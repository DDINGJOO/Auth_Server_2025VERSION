package com.teambiund.bander.auth_server.event.events;

import com.teambiund.bander.auth_server.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberUpdateRequest {

  @NotBlank(message = "사용자 ID는 필수입니다")
  private String userId;

  @NotBlank(message = "전화번호는 필수입니다")
  @PhoneNumber
  private String phoneNumber;
}
