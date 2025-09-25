package com.teambiund.bander.auth_server.controller;

import com.teambiund.bander.auth_server.dto.request.LoginRequest;
import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class LonginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return loginService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

    @PostMapping("/refreshToken")
    public LoginResponse refreshToken(@RequestBody LoginRequest loginRequest) {
        return loginService.refreshToken(loginRequest.getEmail(), loginRequest.getPassword());
    }
}
