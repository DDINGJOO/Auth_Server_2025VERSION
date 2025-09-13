package com.teambiund.bander.auth_server.event.events;


import lombok.*;
import org.springframework.stereotype.Service;

@Data
@Service
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberUpdateRequest {
    private String phoneNumber;
    private String userId;


}
