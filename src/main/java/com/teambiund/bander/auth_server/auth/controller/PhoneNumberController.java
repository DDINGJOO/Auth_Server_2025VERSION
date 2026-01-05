package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.PhoneNumberControllerSwagger;
import com.teambiund.bander.auth_server.auth.service.auth_service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/phone-number")
public class PhoneNumberController implements PhoneNumberControllerSwagger {

  private final AuthService authService;

  @Override
  @GetMapping("/{userId}")
  public ResponseEntity<Boolean> hasPhoneNumber(@PathVariable(name = "userId") String userId) {
    return ResponseEntity.ok(authService.hasPhoneNumber(userId));
  }
}
