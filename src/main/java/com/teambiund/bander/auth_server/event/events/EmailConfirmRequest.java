package com.teambiund.bander.auth_server.event.events;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailConfirmRequest {
    private String email;
    private String code;
}
