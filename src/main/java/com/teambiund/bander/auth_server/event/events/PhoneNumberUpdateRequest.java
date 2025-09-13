package com.teambiund.bander.auth_server.event.events;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Data
@Service
@Getter
@Setter
public class PhoneNumberUpdateRequest {
    private String phoneNumber;
    private String userId;
}
