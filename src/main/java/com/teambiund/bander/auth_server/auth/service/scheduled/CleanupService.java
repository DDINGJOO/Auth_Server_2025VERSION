package com.teambiund.bander.auth_server.auth.service.scheduled;

import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {
  private final AuthRepository authRepository;

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  @SchedulerLock(
      name = "deleteWithdrawUserAfterThreeYears",
      lockAtMostFor = "10m",
      lockAtLeastFor = "1m")
  @Transactional
  public void deleteWithdrawUserAfterThreeYears() {
    log.info("스케쥴 권한 휙득 탈퇴 유저 정리르 시작합니다. ");
    authRepository.deleteByDeletedAtBefore((LocalDateTime.now()));
  }
}
