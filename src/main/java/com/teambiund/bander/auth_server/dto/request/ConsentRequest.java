package com.teambiund.bander.auth_server.dto.request;


import com.teambiund.bander.auth_server.enums.ConsentType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ConsentRequest {
    private final ConsentType consent;
    private final String consentUrl;
    private final boolean consented;
}
