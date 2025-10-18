package com.teambiund.bander.auth_server.service.auth_service;

import com.teambiund.bander.auth_server.dto.response.SimpleAuthResponse;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
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
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
  }
}
