package com.teambiund.bander.auth_server.service.suspend;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.SuspendRepository;
import com.teambiund.bander.auth_server.service.signup.SuspendedService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
@Slf4j
public class SuspendedServiceTest {
    @Autowired
    private SuspendedService suspendedService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private SuspendRepository suspendRepository;

    @BeforeEach
    void setUp() {
        Auth suspended = authRepository.save(
                Auth.builder()
                        .id("suspendedUserId")
                        .email("suspendedUserEmail@email.com")
                        .userRole(Role.GUEST)
                        .status(Status.ACTIVE)
                        .build()
        );

        Auth suspender = authRepository.save(
                Auth.builder()
                        .id("suspenderId")
                        .email("suspenderEmail@email.com")
                        .userRole(Role.ADMIN)
                        .status(Status.ACTIVE)
                        .build()
        );

        authRepository.save(suspended);
        authRepository.save(suspender);
    }

    @AfterEach
    void tearDown() {
        suspendRepository.deleteAll();
        authRepository.deleteAll();
    }


    @Test
    @DisplayName("유저 정지 Test")
    public void suspendUserService() throws CustomException {
        Auth suspended = authRepository.findById("suspendedUserId").orElseThrow();
        Auth suspender = authRepository.findById("suspenderId").orElseThrow();

        suspendedService.suspend(suspended.getId(), "규칙위반", suspender.getId(), 7L);
        suspendRepository.flush();


        Suspend suspend = suspendRepository.findById(suspended.getId()).orElseThrow();
        assertThat(suspend.getReason()).isEqualTo("규칙위반");
        assertThat(suspend.getSuspenderUserId()).isEqualTo(suspender.getId());
        assertThat(suspend.getSuspendUntil()).isEqualTo(suspend.getSuspendAt().plusDays(7));
        assertThat(suspend.getSuspendedUserId()).isEqualTo(suspended.getId());
        Auth suspendedAfter = authRepository.findById("suspendedUserId").orElseThrow();
        assertThat(suspendedAfter.getStatus()).isEqualTo(Status.BLOCKED);
    }

    @Test
    @DisplayName("유저 정지 테스트 어드민 유저가 아닌유저가 정지를 요청할 때 저장을 하지 않고 실패")
    public void suspendUserService_notAdmin() throws CustomException {
        Auth suspended = authRepository.findById("suspendedUserId").orElseThrow();
        Auth suspender = authRepository.findById("suspenderId").orElseThrow();
        suspender.setUserRole(Role.GUEST);
        authRepository.save(suspender);

        assertThrows(CustomException.class, () -> suspendedService.suspend(suspended.getId(), "규칙위반", suspender.getId(), 7L));
        List<Suspend> suspendList = suspendRepository.findAll();
        assertThat(suspendList.size()).isEqualTo(0);
    }


    @Test
    @DisplayName("유저 정지 해제 서비스")
    public void releaseUserService() throws CustomException {

        //given
        Auth suspended = authRepository.findById("suspendedUserId").orElseThrow();
        suspended.setStatus(Status.BLOCKED);
        authRepository.save(suspended);
        suspendRepository.flush();
        assertThat(suspended.getStatus()).isEqualTo(Status.BLOCKED);

        //when
        suspendedService.release(suspended.getId());
        suspendRepository.flush();
        Auth released = authRepository.findById("suspendedUserId").orElseThrow();

        //then
        assertThat(released.getStatus()).isEqualTo(Status.ACTIVE);
    }


}
