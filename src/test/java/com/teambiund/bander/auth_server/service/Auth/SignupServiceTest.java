package com.teambiund.bander.auth_server.service.Auth;

import com.teambiund.bander.auth_server.AuthServerApplication;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



@SpringBootTest(
        classes = AuthServerApplication.class
)

class SignupServiceTest {


    @Autowired
    private SignupService signupService;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private BCryptUtil encoder;

    @AfterEach
    void tearDown() {
        authRepository.deleteAll();
    }

    @Test
    @DisplayName("SignupTest : 회원가입 확인 테스트")
    void signup() throws CustomException {
        SignupRequest req = new SignupRequest();
        req.setEmail("test@test.com");
        req.setPassword("hoss1001!");
        req.setPasswordConfirm("hoss1001!");


        signupService.signup(req.getEmail(), req.getPassword(), req.getPasswordConfirm());
        Auth  auth = authRepository.findByEmail(req.getEmail()).orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));
        assertEquals(req.getEmail(), auth.getEmail());
        assertTrue(encoder.matches(req.getPassword(), auth.getPassword()));
        assertEquals(Status.UNCONFIRMED, auth.getStatus());
        assertEquals(0, auth.getVersion());
    }


}
