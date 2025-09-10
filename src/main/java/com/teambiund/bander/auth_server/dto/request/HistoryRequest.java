package com.teambiund.bander.auth_server.dto.request;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.History;
import com.teambiund.bander.auth_server.util.key_gerneratre.impl.Snowflake;
import lombok.*;

import java.time.LocalDateTime;

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

    public static History toHistory(HistoryRequest request) {
        return History.builder()
                .user(request.getAuth())
                .beforeColumnValue(request.getBeforeValue())
                .afterColumnValue(request.getAfterValue())
                .updatedAt(LocalDateTime.now())
                .id(new Snowflake().generateKey())
                .updatedColumn(request.getUpdatedColumn())
                .build();
    }
}
