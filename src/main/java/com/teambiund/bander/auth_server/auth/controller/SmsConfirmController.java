package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.SmsConfirmControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.SmsCodeRequest;
import com.teambiund.bander.auth_server.auth.dto.request.SmsVerifyRequest;
import com.teambiund.bander.auth_server.auth.service.update.SmsConfirmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/sms")
@RequiredArgsConstructor
public class SmsConfirmController implements SmsConfirmControllerSwagger {

  private final SmsConfirmService smsConfirmService;

  @Override
  @PostMapping("/request")
  public void generateCode(@Valid @RequestBody SmsCodeRequest request) {
    smsConfirmService.generateCode(request.getUserId(), request.getPhoneNumber());
  }

  @Override
  @PostMapping("/verify")
  public boolean confirmSms(@Valid @RequestBody SmsVerifyRequest request) {
    return smsConfirmService.confirmSms(
        request.getUserId(), request.getPhoneNumber(), request.getCode());
  }

  @Override
  @PostMapping("/resend")
  public boolean resendSms(@Valid @RequestBody SmsCodeRequest request) {
    return smsConfirmService.resendSms(request.getUserId(), request.getPhoneNumber());
  }
}
