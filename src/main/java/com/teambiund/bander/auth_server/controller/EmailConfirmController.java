package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.service.update.EmailConfirm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/emails")
@RequiredArgsConstructor
public class EmailConfirmController {
  private final EmailConfirm emailConfirm;

  @GetMapping("/{email}")
  public boolean confirmEmail(
      @RequestParam String code, @PathVariable(name = "email") String email) {
    return emailConfirm.confirmEmail(code, email);
  }

  @PostMapping("/{email}")
  public void generateCode(@PathVariable(name = "email") String email) {
    emailConfirm.generateCode(email);
  }
}
