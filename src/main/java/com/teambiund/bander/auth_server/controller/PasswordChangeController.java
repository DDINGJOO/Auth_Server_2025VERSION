package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.service.ReadAndPost.password_change.PasswordChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/passwordChange")
@RequiredArgsConstructor
public class PasswordChangeController
{
    private final PasswordChangeService passwordChangeService;

    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> changePassword(String email, String newPassword, String passConfirm) throws Exception {
        passwordChangeService.changePassword(email, newPassword, passConfirm);
        return ResponseEntity.ok(true);
    }
}
