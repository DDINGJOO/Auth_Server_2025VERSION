package com.teambiund.bander.auth_server.auth.service.update.impl;

import com.teambiund.bander.auth_server.auth.event.events.PhoneNumberUpdateRequest;
import com.teambiund.bander.auth_server.auth.event.events.PhoneNumberVerifiedEvent;
import com.teambiund.bander.auth_server.auth.event.events.SmsConfirmRequest;
import com.teambiund.bander.auth_server.auth.event.publish.PhoneNumberVerifiedEventPub;
import com.teambiund.bander.auth_server.auth.event.publish.SmsConfirmRequestEventPub;
import com.teambiund.bander.auth_server.auth.service.update.PhoneNumberUpdateService;
import com.teambiund.bander.auth_server.auth.service.update.SmsConfirmService;
import com.teambiund.bander.auth_server.auth.util.generator.generate_code.SmsCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsConfirmServiceImpl implements SmsConfirmService {

  private final SmsCodeGenerator smsCodeGenerator;
  private final SmsConfirmRequestEventPub smsConfirmRequestEventPub;
  private final PhoneNumberVerifiedEventPub phoneNumberVerifiedEventPub;
  private final PhoneNumberUpdateService phoneNumberUpdateService;

  @Override
  public void generateCode(String userId, String phoneNumber) {
    String code = smsCodeGenerator.generateCode(userId, phoneNumber);
    smsConfirmRequestEventPub.publish(new SmsConfirmRequest(phoneNumber, code));
    log.info("SMS 인증 코드 이벤트 발행 완료 - userId: {}", userId);
  }

  @Override
  public boolean confirmSms(String userId, String phoneNumber, String code) {
    boolean isValid = smsCodeGenerator.checkCode(userId, phoneNumber, code);

    if (isValid) {
      // 인증 성공 시 전화번호 암호화 저장
      savePhoneNumber(userId, phoneNumber);
    }

    return isValid;
  }

  @Override
  public boolean resendSms(String userId, String phoneNumber) {
    if (smsCodeGenerator.canResend(userId, phoneNumber)) {
      generateCode(userId, phoneNumber);
      return true;
    }

    // 기존 코드가 있으면 삭제 후 재발신
    smsCodeGenerator.deleteCode(userId, phoneNumber);
    generateCode(userId, phoneNumber);
    return true;
  }

  @Override
  public void savePhoneNumber(String userId, String phoneNumber) {
    PhoneNumberUpdateRequest req = new PhoneNumberUpdateRequest(userId, phoneNumber);
    phoneNumberUpdateService.updatePhoneNumber(req);
    log.info("전화번호 저장 완료 - userId: {}", userId);

    // Notification Service에 전화번호 인증 완료 이벤트 발행
    phoneNumberVerifiedEventPub.publish(new PhoneNumberVerifiedEvent(userId, phoneNumber));
    log.info("전화번호 인증 완료 이벤트 발행 - userId: {}", userId);
  }
}