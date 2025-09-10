package com.teambiund.bander.auth_server.service.confirmed;

import com.teambiund.bander.auth_server.AuthServerApplication;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = AuthServerApplication.class
)
public class EmailConfirmTest {

    @Autowired
    private UpdateService updateService;
    @Autowired
    private AuthRepository authRepository;

    @BeforeEach
    void setUp() {
        Auth auth = Auth.builder()
                .id("test")
                .email("test@example.com")
                .password("password")
                .status(Status.UNCONFIRMED)
                .userRole(Role.GUEST)
                .build();

        authRepository.save(auth);
    }

    @Test
    @DisplayName("이메일 인증 확인 테스트 ")
    void testEmailConfirm() throws CustomException {
        updateService.EmailConfirm("test");
        Auth auth = authRepository.findById("test").orElseThrow();

        assert auth.getStatus().equals(Status.ACTIVE);
        assert auth.getUserRole().equals(Role.USER);
    }

}
