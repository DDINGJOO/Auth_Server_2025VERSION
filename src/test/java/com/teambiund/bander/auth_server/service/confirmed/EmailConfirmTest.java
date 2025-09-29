package com.teambiund.bander.auth_server.service.confirmed;

import com.teambiund.bander.auth_server.AuthServerApplication;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = AuthServerApplication.class)
public class EmailConfirmTest {

    @Autowired
    private UpdateService updateService;
    @Autowired
    private AuthRepository authRepository;

    @BeforeEach
    void setUp() {
        authRepository.deleteAll();
        Auth auth = Auth.builder()
                .id("test")
                .email("test@example.com")
                .password("password")
                .status(Status.UNCONFIRMED)
                .userRole(Role.GUEST)
                .build();
        authRepository.save(auth);
    }

    @AfterEach
    void cleanUp() {
        authRepository.deleteAll();
    }

    @Test
    @DisplayName("EmailConfirm: 정상 인증 시 상태/권한 변경")
    void emailConfirm_success() throws CustomException {
        // when
        updateService.EmailConfirm("test");

        // then
        Auth auth = authRepository.findById("test").orElseThrow();
        assertEquals(Status.ACTIVE, auth.getStatus());
        assertEquals(Role.USER, auth.getUserRole());
    }

    @Test
    @DisplayName("EmailConfirm: 존재하지 않는 사용자 -> USER_NOT_FOUND 예외")
    void emailConfirm_userNotFound() {
        // given - 'no-user' does not exist
        String invalidUserId = "no-user";

        // expect
        CustomException ex = assertThrows(CustomException.class, () -> updateService.EmailConfirm(invalidUserId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("EmailConfirm: 이미 ACTIVE/USER인 경우에도 안전하게 동작 (idemPotent)")
    void emailConfirm_alreadyConfirmed_idempotent() throws CustomException {
        // given
        Auth auth = authRepository.findById("test").orElseThrow();
        auth.setStatus(Status.ACTIVE);
        auth.setUserRole(Role.USER);
        authRepository.save(auth);

        // when (call again)
        updateService.EmailConfirm("test");

        // then - remains ACTIVE/USER
        Auth updated = authRepository.findById("test").orElseThrow();
        assertEquals(Status.ACTIVE, updated.getStatus());
        assertEquals(Role.USER, updated.getUserRole());
    }
}
