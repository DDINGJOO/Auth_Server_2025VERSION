package com.teambiund.bander.auth_server.service.scheduled;


import com.teambiund.bander.auth_server.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupService {
    private final AuthRepository authRepository;


    public void deleteWithdrawUserAfterThreeYears() {
        authRepository.deleteByDeletedAtBefore((LocalDateTime.now()));
    }
}
