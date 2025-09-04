package com.teambiund.bander.auth_server.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class SignupRequest
{

    private String email;
    private String password;
    private String passwordConfirm;
}
