package com.teambiund.bander.auth_server.dto.request;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.History;
import com.teambiund.bander.auth_server.util.generator.key.impl.Snowflake;
import java.time.LocalDateTime;
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
