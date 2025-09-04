package com.teambiund.bander.auth_server.controller;


import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.ReadAndPost.Regist.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/withdraw")
@RequiredArgsConstructor
public class WithdrawController
{
    public final WithdrawService withdrawService;

    @PostMapping("/withdraw")
    public ResponseEntity<Boolean> withdraw(String userId, String withdrawReason) throws CustomException {
        withdrawService.withdraw(userId, withdrawReason);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/withdrawRetraction")
    public ResponseEntity<Boolean> withdrawRetraction(String email) throws CustomException {
        withdrawService.withdrawRetraction(email);
        return ResponseEntity.ok(true);
    }
}
