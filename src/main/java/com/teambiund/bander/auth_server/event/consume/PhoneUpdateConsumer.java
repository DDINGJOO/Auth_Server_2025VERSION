package com.teambiund.bander.auth_server.event.consume;


import com.teambiund.bander.auth_server.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.update.PhoneNumberUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class PhoneUpdateConsumer {
    private final PhoneNumberUpdateService phoneNumberUpdateService;

    private void changePhoneNumber(PhoneNumberUpdateRequest event) {
        try {
            phoneNumberUpdateService.updatePhoneNumber(event);
        } catch (CustomException e) {
            log.error("Phone number update failed : {}", e.getMessage());
        }
    }
}
