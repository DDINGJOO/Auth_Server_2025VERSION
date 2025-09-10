package com.teambiund.bander.auth_server.controller.schedule;


import com.teambiund.bander.auth_server.service.scheduled.CleanupService;
import com.teambiund.bander.auth_server.service.scheduled.SuspendRelease;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping("/api/auth/schedule")
public class ScheduleController {
    private final CleanupService cleanupService;
    private final SuspendRelease suspendRelease;

    //k8s 에서 crown Job을 통해 실행
    @GetMapping()
    public void schedule() {
        log.info("ScheduleController.schedule()");
        deleteUserAfterThreeYears();
        SuspendRelease();

    }

    private void deleteUserAfterThreeYears() {
        cleanupService.deleteWithdrawUserAfterThreeYears();
    }

    private void SuspendRelease() {
        suspendRelease.release();
    }

}
