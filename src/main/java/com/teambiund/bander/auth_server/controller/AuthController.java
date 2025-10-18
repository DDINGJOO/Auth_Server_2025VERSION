package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.response.SimpleAuthResponse;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.auth_service.AuthService;
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
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @GetMapping("{userId}")
  public ResponseEntity<SimpleAuthResponse> getAuth(@PathVariable(name = "userId") String userId)
      throws CustomException {
    return ResponseEntity.ok(authService.getAuth(userId));
  }

  //    @PutMapping("/email-confirm") // userId from gateWay
  //    public ResponseEntity<Boolean> emailConfirm(@RequestParam String code, @RequestParam String
  // userId) throws CustomException {
  //        String userIdByCode = redisUtil.checkCode(code);
  //        if (userIdByCode == null) {
  //            throw new CustomException(ErrorCode.INVALID_CODE);
  //        }
  //        if (!userId.equals(userIdByCode)) {
  //            throw new CustomException(ErrorCode.INVALID_USER);
  //        }
  //        updateService.EmailConfirm(userId);
  //        return ResponseEntity.ok(true);
  //    }

}
