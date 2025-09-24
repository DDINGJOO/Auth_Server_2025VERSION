package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.LoginStatus;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final LoginStatusRepository loginStatusRepository;
    private AuthRepository authRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public void login(String email, String password) {
        Auth auth = authRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }


        auth.setLoginStatus(LoginStatus.builder()
                .auth(auth)
                .lastLogin(LocalDateTime.now())
                .build()
        );

        authRepository.save(auth);


    }
}
