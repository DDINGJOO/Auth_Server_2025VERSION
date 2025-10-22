package com.teambiund.bander.auth_server.auth.event.consume;

import com.teambiund.bander.auth_server.auth.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.service.update.PhoneNumberUpdateService;
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
