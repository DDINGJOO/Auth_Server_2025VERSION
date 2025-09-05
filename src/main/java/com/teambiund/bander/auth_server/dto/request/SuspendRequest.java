package com.teambiund.bander.auth_server.dto.request;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspendRequest {
    private String suspendReason;
    private String suspenderUserId;
    private String suspendedUserId;
    private LocalDate suspendUntil;
}
