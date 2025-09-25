package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.LoginStatus;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.util.generator.token.TokenUtil;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final LoginStatusRepository loginStatusRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;
    private final TokenStoreService tokenStoreService;

    @Override
    public LoginResponse login(String email, String password) {
        Auth auth = authRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
        return generateResponse(auth);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken, String deviceId) {
        if (!tokenUtil.isValid(refreshToken)) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
        String userId = tokenUtil.extractUserId(refreshToken);
        String deviceIdFromToken = tokenUtil.extractDeviceId(refreshToken);
        if (userId == null || deviceIdFromToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (!deviceId.equals(deviceIdFromToken)) {
            throw new CustomException(ErrorCode.INVALID_DEVICE_ID);
        }

        Auth auth = authRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        return generateResponse(auth);

    }

    private LoginResponse generateResponse(Auth auth) {

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

        String deviceId = UUID.randomUUID().toString().substring(0, 4);
        String accessToken = tokenUtil.generateAccessToken(auth.getId(), auth.getUserRole(), deviceId);
        String refreshToken = tokenUtil.generateRefreshToken(auth.getId(), auth.getUserRole(), deviceId);
        tokenStoreService.TokenStored(accessToken);

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
