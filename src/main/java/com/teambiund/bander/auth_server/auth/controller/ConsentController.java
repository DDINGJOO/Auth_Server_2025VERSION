package com.teambiund.bander.auth_server.auth.controller;

import com.teambiund.bander.auth_server.auth.controller.swagger.ConsentControllerSwagger;
import com.teambiund.bander.auth_server.auth.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.service.consent.ConsentManagementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/consent")
@RequiredArgsConstructor
public class ConsentController implements ConsentControllerSwagger {

  private final ConsentManagementService consentService;

  @Override
  @PatchMapping("/{userId}")
  public ResponseEntity<Boolean> consent(
      @PathVariable(name = "userId") String userId, @RequestBody List<ConsentRequest> requests)
      throws CustomException {
    consentService.changeConsent(userId, requests);
    return ResponseEntity.ok(true);
  }
}
