package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignupController {
  private final SignupService signupStoreService;

  @PostMapping("/signup")
  public ResponseEntity<Boolean> signup(@Valid @RequestBody SignupRequest req)
      throws CustomException {

    signupStoreService.signup(
        req.getEmail(), req.getPassword(), req.getPasswordConfirm(), req.getConsentReqs());
    return ResponseEntity.ok(true);
  }
}
