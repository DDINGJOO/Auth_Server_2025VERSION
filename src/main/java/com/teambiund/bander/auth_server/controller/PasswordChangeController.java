package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.service.update.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/passwordChange")
@RequiredArgsConstructor
public class PasswordChangeController
{
    private final UpdateService updateService;

    @PostMapping("/changePassword")
    //TODO : CHANGE REQUEST PARAM TO OBJECT
    public ResponseEntity<Boolean> changePassword(@RequestParam String email, @RequestParam String newPassword, @RequestParam String passConfirm) throws Exception {
        updateService.changePassword(email, newPassword, passConfirm);
        return ResponseEntity.ok(true);
    }
}
