package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.ReadAndPost.Regist.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignupController
{
    private final SignupService signupService;
    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@RequestBody SignupRequest req) throws CustomException {
        signupService.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm());
        return ResponseEntity.ok(true);
    }
}
