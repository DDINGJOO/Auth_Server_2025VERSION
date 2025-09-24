package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.LoginStatus;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.util.generator.token_generator.TokenProvider;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final LoginStatusRepository loginStatusRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public LoginResponse login(String email, String password) {
        Auth auth = authRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (auth.getStatus().equals(Status.ACTIVE)) {
            switch (auth.getStatus()) {
                case SLEEPING:
                    throw new CustomException(ErrorCode.USER_IS_SLEEPING);
                case BLOCKED:
                    throw new CustomException(ErrorCode.USER_IS_BLOCKED);
                case SUSPENDED:
                    throw new CustomException(ErrorCode.USER_IS_SUSPENDED);
                case DELETED:
                    throw new CustomException(ErrorCode.USER_IS_DELETED);
                default:
                    break;
            }
        }


        String accessToken = tokenProvider.generateAccessToken(auth.getId(), auth.getUserRole());
        String refreshToken = tokenProvider.generateRefreshToken(auth.getId(), auth.getUserRole());


        var response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);


        loginStatusRepository.save(
                LoginStatus.builder()
                        .auth(auth)
                        .lastLogin(LocalDateTime.now())
                        .build()
        );
        return response;
    }


}
