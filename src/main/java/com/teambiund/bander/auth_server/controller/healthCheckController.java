package com.teambiund.bander.auth_server.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class healthCheckController {

    @GetMapping
    public String healthCheck() {
        return "Server is up and running";
    }

}
