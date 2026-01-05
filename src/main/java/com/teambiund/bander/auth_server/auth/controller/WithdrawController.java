package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.WithdrawControllerSwagger;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.service.withdrawal.WithdrawalManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/withdraw")
@RequiredArgsConstructor
public class WithdrawController implements WithdrawControllerSwagger {

  private final WithdrawalManagementService withdrawService;

  @Override
  @PostMapping("/{userId}")
  public ResponseEntity<Boolean> withdraw(
      @PathVariable(name = "userId") String userId, @RequestParam String withdrawReason)
      throws CustomException {
    withdrawService.withdraw(userId, withdrawReason);
    return ResponseEntity.ok(true);
  }

  @Override
  @PostMapping("/withdrawRetraction")
  public ResponseEntity<Boolean> withdrawRetraction(@RequestParam String email)
      throws CustomException {
    withdrawService.withdrawRetraction(email);
    return ResponseEntity.ok(true);
  }
}
