package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class PasswordChangeServiceTest {
    @Autowired
    private PasswordChangeService passwordChangeService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private BCryptUtil encoder;

    @BeforeEach
    void setUp() {
        authRepository.save(
                Auth.builder()
                        .email("test@example.com")
                        .password("password")
                        .status(Status.UNCONFIRMED)
                        .version(0)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        authRepository.deleteAll();
    }


    @Test
    @DisplayName("PasswordChangeService : 비밀번호 변경 서비스 (비정상 비밀번호)")
    void passwordChange() throws CustomException {
        Auth auth = authRepository.findByEmail("test@example.com").orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String newPassword = "hos";
        assertThrows(CustomException.class, () -> passwordChangeService.changePassword(auth.getEmail(), newPassword, newPassword));
        assertEquals(Status.UNCONFIRMED, auth.getStatus());
        assertEquals(0, auth.getVersion());
        assertEquals(encoder.encode(newPassword), auth.getPassword());
        assertEquals(LocalDateTime.now(), auth.getUpdatedAt());
    }
}
