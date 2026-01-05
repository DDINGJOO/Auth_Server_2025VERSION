package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.PasswordChangeControllerSwagger;
import com.teambiund.bander.auth_server.auth.service.update.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordChangeController implements PasswordChangeControllerSwagger {

  private final UpdateService updateService;

  @Override
  @PostMapping("")
  public ResponseEntity<Boolean> changePassword(
      @RequestParam String email, @RequestParam String newPassword, @RequestParam String passConfirm)
      throws Exception {
    updateService.changePassword(email, newPassword, passConfirm);
    return ResponseEntity.ok(true);
  }
}
