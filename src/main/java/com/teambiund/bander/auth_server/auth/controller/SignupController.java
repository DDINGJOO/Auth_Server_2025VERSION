package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.SignupControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.service.signup.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SignupController implements SignupControllerSwagger {

  private final SignupService signupStoreService;

  @Override
  @PostMapping("/signup")
  public ResponseEntity<Boolean> signup(@Valid @RequestBody SignupRequest req)
      throws CustomException {
    signupStoreService.signup(req);
    return ResponseEntity.ok(true);
  }
}
