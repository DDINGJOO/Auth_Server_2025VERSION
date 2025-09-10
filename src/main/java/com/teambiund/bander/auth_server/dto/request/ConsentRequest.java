package com.teambiund.bander.auth_server.dto.request;


import com.teambiund.bander.auth_server.enums.ConsentType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ConsentRequest {
    private ConsentType consent;
    private String consentUrl;
    private boolean consented;
}
