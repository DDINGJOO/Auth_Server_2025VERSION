package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.RoleControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.RoleChangeRequest;
import com.teambiund.bander.auth_server.auth.service.auth_service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/v1/auth/role")
@RequiredArgsConstructor
public class RoleController implements RoleControllerSwagger {

  private final AuthService authService;

  @Override
  @PutMapping("")
  public ResponseEntity<Boolean> changeRole(@Valid @RequestBody RoleChangeRequest request) {
    authService.changeRole(request.getEmail(), request.getRole());
    return ResponseEntity.ok(true);
  }
}
