package com.teambiund.bander.auth_server.auth.dto.request;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRequest {
  public Auth auth;
  public String beforeValue;
  public String afterValue;
  public String updatedColumn;
}
