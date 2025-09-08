package com.teambiund.bander.auth_server.controller;


import com.teambiund.bander.auth_server.dto.request.SuspendRequest;
import com.teambiund.bander.auth_server.service.signup.SuspendedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/admin/auth/suspend")
@RestController
@RequiredArgsConstructor
public class SuspendController {
    private final SuspendedService suspendedService;

    @PostMapping("")
    public ResponseEntity<Boolean> suspend(@RequestBody SuspendRequest req) throws Exception {
        suspendedService.suspend( req.getSuspendedUserId(), req.getSuspendReason(), req.getSuspenderUserId());
        return ResponseEntity.ok(true);
    }

    @GetMapping("/release")
    public ResponseEntity<Boolean> unsuspend(@RequestParam String userId) throws Exception {
        suspendedService.release(userId);
        return ResponseEntity.ok(true);
    }
}

