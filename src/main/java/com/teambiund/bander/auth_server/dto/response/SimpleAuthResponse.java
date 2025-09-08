package com.teambiund.bander.auth_server.dto.response;


import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleAuthResponse {
    private String userId;
    private Status status;
    private Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static SimpleAuthResponse from(Auth auth) {
        return SimpleAuthResponse.builder()
                .userId(auth.getId())
                .status(auth.getStatus())
                .provider(auth.getProvider())
                .createdAt(auth.getCreatedAt())
                .updatedAt(auth.getUpdatedAt())
                .build();
    }


}
