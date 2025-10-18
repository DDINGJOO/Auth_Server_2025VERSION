package com.teambiund.bander.auth_server.service.scheduled;

import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.SuspendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SuspendRelease {
    private final SuspendRepository suspendRepository;
    private final AuthRepository authRepository;


    /**
     * 정지기간이 끝난 유저 정지 해제
     * - 연관관계를 활용하여 N+1 문제 해결
     * - orphanRemoval=true 설정으로 Suspend 엔티티 자동 삭제
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "releaseUserAfter suspended Until", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void release() {
        log.info("스케쥴 권한 획득 정지기간이 지난 유저 정지 해제를 시작합니다. ");
        List<Suspend> suspends = suspendRepository.findAllBySuspendUntilIsBefore((LocalDate.now().minusDays(1)));

        // Suspend 엔티티에서 직접 Auth를 가져와서 처리 (N+1 문제 해결)
        suspends.forEach(suspend -> {
            var auth = suspend.getSuspendedUser();
            auth.setStatus(Status.ACTIVE);

            // orphanRemoval=true로 인해 suspensions 컬렉션에서 제거하면 Suspend도 자동 삭제됨
            auth.getSuspensions().remove(suspend);
            authRepository.save(auth);
        });
    }

}
