package com.teambiund.bander.auth_server.controller;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.signup.ConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth/consent")
@RequiredArgsConstructor
public class ConsentController {
    private final ConsentService consentService;

    @PutMapping
    public ResponseEntity<Boolean> consent(String userId, List<ConsentRequest> requests) throws CustomException {
        consentService.changeConsent(userId, requests);
        return ResponseEntity.ok(true);
    }
}
