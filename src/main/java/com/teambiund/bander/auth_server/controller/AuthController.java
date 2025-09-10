package com.teambiund.bander.auth_server.controller;


import com.teambiund.bander.auth_server.dto.response.SimpleAuthResponse;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.service.auth_service.AuthService;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import com.teambiund.bander.auth_server.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RedisUtil redisUtil;
    private final UpdateService updateService;


    @GetMapping("")
    public ResponseEntity<SimpleAuthResponse> getAuth(@RequestParam String userId) throws CustomException {
        return ResponseEntity.ok(authService.getAuth(userId));
    }

    @PutMapping("/email-confirm")
    public ResponseEntity<Boolean> emailConfirm(@RequestParam String code) throws CustomException {
        String userId = redisUtil.checkCode(code);
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_CODE);
        }
        updateService.EmailConfirm(userId);
        return ResponseEntity.ok(true);
    }

}
