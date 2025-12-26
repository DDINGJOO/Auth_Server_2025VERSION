package com.teambiund.bander.auth_server.auth.service.login;

import com.teambiund.bander.auth_server.auth.dto.response.LoginResponse;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.LoginStatus;
import com.teambiund.bander.auth_server.auth.enums.AppType;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.repository.LoginStatusRepository;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.auth.util.generator.token.TokenUtil;
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
      @Qualifier("aesCipherStrategy") CipherStrategy emailCipher) {
    this.loginStatusRepository = loginStatusRepository;
    this.authRepository = authRepository;
    this.keyProvider = keyProvider;
    this.passwordEncoder = passwordEncoder;
    this.tokenUtil = tokenUtil;
    this.emailCipher = emailCipher;
  }

  @Override
  public LoginResponse login(String email, String password, AppType appType) {
    String encryptedEmail = emailCipher.encrypt(email);
    Auth auth =
        authRepository
            .findByEmailWithLoginStatus(encryptedEmail)
            .or(
                () ->
                    authRepository.findByEmailWithLoginStatus(
                        email)) // Backward-compatibility for legacy plaintext rows
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    if (!passwordEncoder.matches(password, auth.getPassword())) {
      throw new CustomException(AuthErrorCode.PASSWORD_MISMATCH);
    }

    // AppType에 따른 접근 권한 검증
    validateAppTypeAccess(auth.getUserRole(), appType);

    return generateResponse(auth);
  }

  @Override
  public LoginResponse refreshToken(String refreshToken, String deviceId, AppType appType) {
    if (!tokenUtil.isValid(refreshToken)) {
      throw new CustomException(AuthErrorCode.EXPIRED_TOKEN);
    }
    String userId = tokenUtil.extractUserId(refreshToken);
    String deviceIdFromToken = tokenUtil.extractDeviceId(refreshToken);
    if (userId == null || deviceIdFromToken == null) {
      throw new CustomException(AuthErrorCode.INVALID_TOKEN);
    }

    if (!deviceId.equals(deviceIdFromToken)) {
      throw new CustomException(AuthErrorCode.INVALID_DEVICE_ID);
    }

    Auth auth =
        authRepository
            .findByIdWithLoginStatus(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

    // AppType에 따른 접근 권한 검증
    validateAppTypeAccess(auth.getUserRole(), appType);

    return generateResponse(auth);
  }

  private void validateAppTypeAccess(Role userRole, AppType appType) {
    if (appType == AppType.PLACE_MANAGER) {
      // 공간관리자 앱: PLACE_OWNER만 허용
      if (userRole != Role.PLACE_OWNER) {
        throw new CustomException(AuthErrorCode.UNAUTHORIZED_APP_ACCESS);
      }
    }
    // GENERAL 앱: USER, GUEST, PLACE_OWNER 모두 허용 (별도 검증 불필요)
  }

  private LoginResponse generateResponse(Auth auth) {

    if (!auth.getStatus().equals(Status.ACTIVE)) {
      switch (auth.getStatus()) {
        case SLEEPING:
          throw new CustomException(AuthErrorCode.USER_IS_SLEEPING);
        case BLOCKED:
          throw new CustomException(AuthErrorCode.USER_IS_BLOCKED);
        case SUSPENDED:
          throw new CustomException(AuthErrorCode.USER_IS_SUSPENDED);
        case DELETED:
          throw new CustomException(AuthErrorCode.USER_IS_DELETED);
        default:
          break;
      }
    }

    String deviceId = UUID.randomUUID().toString().substring(0, 4);
    String accessToken = tokenUtil.generateAccessToken(auth.getId(), auth.getUserRole(), deviceId);
    String refreshToken =
        tokenUtil.generateRefreshToken(auth.getId(), auth.getUserRole(), deviceId);

    var response = new LoginResponse();
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    response.setDeviceId(deviceId);

    // LoginStatus 생성 또는 업데이트
    if (auth.getLoginStatus() == null) {
      // LoginStatus가 없는 경우 (기존 사용자 데이터 또는 테스트) 새로 생성
      LoginStatus loginStatus = LoginStatus.builder().build();
      auth.setLoginStatus(loginStatus);
    }
    auth.getLoginStatus().setLastLogin(LocalDateTime.now());

    // CascadeType.ALL로 인해 auth만 save하면 loginStatus도 자동 저장됨
    authRepository.save(auth);

    return response;
  }

  @Override
  public LoginResponse generateLoginResponse(Auth auth) {
    if (!auth.getStatus().equals(Status.ACTIVE)) {
      switch (auth.getStatus()) {
        case SLEEPING:
          throw new CustomException(AuthErrorCode.USER_IS_SLEEPING);
        case BLOCKED:
          throw new CustomException(AuthErrorCode.USER_IS_BLOCKED);
        case SUSPENDED:
          throw new CustomException(AuthErrorCode.USER_IS_SUSPENDED);
        case DELETED:
          throw new CustomException(AuthErrorCode.USER_IS_DELETED);
        default:
          break;
      }
    }
    return generateResponse(auth);
  }
}
