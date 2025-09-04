package com.teambiund.bander.auth_server.controller;


import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/health")
public class healthCheckController {

    @GetMapping
    public String healthCheck() {
        return "Server is up and running";
    }

}
