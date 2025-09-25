package com.teambiund.bander.auth_server.dto.request;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRefreshRequest {
    private String refreshToken;
    private String deviceId;
}
