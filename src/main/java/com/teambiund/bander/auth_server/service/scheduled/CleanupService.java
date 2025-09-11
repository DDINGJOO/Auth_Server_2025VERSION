package com.teambiund.bander.auth_server.service.scheduled;


import com.teambiund.bander.auth_server.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupService {
    private final AuthRepository authRepository;


    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "deleteWithdrawUserAfterThreeYears", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void deleteWithdrawUserAfterThreeYears() {
        authRepository.deleteByDeletedAtBefore((LocalDateTime.now()));
    }
}
