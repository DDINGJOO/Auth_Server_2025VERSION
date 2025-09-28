package com.teambiund.bander.auth_server.dto.request;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ConsentRequest {
    private String consentName;
    private String version;
    private boolean consented;
}
