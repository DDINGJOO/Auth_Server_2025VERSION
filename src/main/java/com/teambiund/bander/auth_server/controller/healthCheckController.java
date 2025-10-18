package com.teambiund.bander.auth_server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class healthCheckController {

  @GetMapping
  public String healthCheck() {
    log.info("Server is up and running");
    return "Server is up and running";
  }
}
