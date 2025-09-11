package com.teambiund.bander.auth_server.service.scheduled;

import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.SuspendRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SuspendRelease {
    private final SuspendRepository suspendRepository;
    private final AuthRepository authRepository;


    //[ 04:00 시에 정지기간이 끝나는 날인 유저 정지 해제 ]
    //TODO : SOLVE N+1 ISSUE
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "releaseUserAfter suspended Until", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void release() {
        List<Suspend> suspends = suspendRepository.findAllBySuspendUntilIsBefore((LocalDate.now().minusDays(1)));
        authRepository.findAllById(suspends.stream().map(Suspend::getSuspendedUserId).toList())
                .forEach(e -> {
                    e.setStatus(Status.ACTIVE);
                    authRepository.save(e);
                    suspendRepository.deleteById(e.getId());
                    authRepository.deleteById(e.getId());
                });
    }

}
