package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.SuspendControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.SuspendRequest;
import com.teambiund.bander.auth_server.auth.service.suspension.SuspensionManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin/v1/auth/suspend")
@RestController
@RequiredArgsConstructor
public class SuspendController implements SuspendControllerSwagger {

  private final SuspensionManagementService suspendedService;

  @Override
  @PostMapping("")
  public ResponseEntity<Boolean> suspend(@Valid @RequestBody SuspendRequest req) throws Exception {
    suspendedService.suspend(
        req.getSuspendedUserId(),
        req.getSuspendReason(),
        req.getSuspenderUserId(),
        req.getSuspendDay());
    return ResponseEntity.ok(true);
  }

  @Override
  @GetMapping("/release")
  public ResponseEntity<Boolean> unsuspend(@RequestParam String userId) throws Exception {
    suspendedService.release(userId);
    return ResponseEntity.ok(true);
  }
}
