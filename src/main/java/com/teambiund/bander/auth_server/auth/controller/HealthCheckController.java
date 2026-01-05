package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.HealthCheckControllerSwagger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthCheckController implements HealthCheckControllerSwagger {

  @Override
  @GetMapping
  public String healthCheck() {
    log.info("Server is up and running");
    return "Server is up and running";
  }
}
