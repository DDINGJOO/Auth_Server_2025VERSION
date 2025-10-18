package com.teambiund.bander.auth_server.service.login;

import com.teambiund.bander.auth_server.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.LoginStatus;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.util.generator.token.TokenUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    private final LoginStatusRepository loginStatusRepository;
    private final AuthRepository authRepository;
    private final KeyProvider keyProvider;
    private final CipherStrategy passwordEncoder;
    private final TokenUtil tokenUtil;
    private final CipherStrategy emailCipher;

    public LoginServiceImpl(
            LoginStatusRepository loginStatusRepository,
            AuthRepository authRepository,
            KeyProvider keyProvider,
            @Qualifier("pbkdf2CipherStrategy") CipherStrategy passwordEncoder,
            TokenUtil tokenUtil,
            @Qualifier("aesCipherStrategy") CipherStrategy emailCipher
    ) {
        this.loginStatusRepository = loginStatusRepository;
        this.authRepository = authRepository;
        this.keyProvider = keyProvider;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
        this.emailCipher = emailCipher;
    }

    @Override
    public LoginResponse login(String email, String password) {
        String encryptedEmail = emailCipher.encrypt(email);
        Auth auth = authRepository.findByEmail(encryptedEmail)
                .or(() -> authRepository.findByEmail(email)) // Backward-compatibility for legacy plaintext rows
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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

        if (!auth.getStatus().equals(Status.ACTIVE)) {
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

        var response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setDeviceId(deviceId);


        // LoginStatus 생성 또는 업데이트
        LoginStatus loginStatus = LoginStatus.builder()
                .lastLogin(LocalDateTime.now())
                .build();

        // 편의 메서드 사용 - 양방향 연관관계 설정
        auth.setLoginStatus(loginStatus);

        // CascadeType.ALL로 인해 auth만 save하면 loginStatus도 자동 저장됨
        authRepository.save(auth);

        return response;

    }


}
