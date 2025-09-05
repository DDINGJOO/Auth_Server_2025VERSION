package com.teambiund.bander.auth_server.controller;


import com.teambiund.bander.auth_server.dto.request.SuspendRequest;
import com.teambiund.bander.auth_server.service.ReadAndPost.Regist.SuspendedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin/auth/suspend")
@RestController
@RequiredArgsConstructor
public class SuspendController {
    private final SuspendedService suspendedService;

    @PostMapping("/suspend")
    public ResponseEntity<Boolean> suspend(@RequestBody SuspendRequest req) throws Exception {
        suspendedService.suspend( req.getSuspendedUserId(), req.getSuspendReason(), req.getSuspenderUserId());
        return ResponseEntity.ok(true);
    }
}

