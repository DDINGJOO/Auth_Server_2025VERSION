package com.teambiund.bander.auth_server.auth.service.auth_service;

import com.teambiund.bander.auth_server.auth.dto.response.SimpleAuthResponse;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

  private final AuthRepository authRepository;

  @Transactional(readOnly = true)
  public SimpleAuthResponse getAuth(String userId) throws CustomException {
    return authRepository
        .findById(userId)
        .map(SimpleAuthResponse::from)
        .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
  }
}
