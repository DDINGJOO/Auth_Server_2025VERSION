package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.EmailConfirmControllerSwagger;
import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/emails")
@RequiredArgsConstructor
public class EmailConfirmController implements EmailConfirmControllerSwagger {

  private final EmailConfirm emailConfirm;

  @Override
  @GetMapping("/{email}")
  public boolean confirmEmail(
      @RequestParam String code, @PathVariable(name = "email") String email) {
    return emailConfirm.confirmEmail(code, email);
  }

  @Override
  @PostMapping("/{email}")
  public void generateCode(@PathVariable(name = "email") String email) {
    emailConfirm.generateCode(email);
  }
}
