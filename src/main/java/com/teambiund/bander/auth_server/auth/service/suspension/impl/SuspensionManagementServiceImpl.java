package com.teambiund.bander.auth_server.auth.service.suspension.impl;

import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.entity.Suspend;
import com.teambiund.bander.auth_server.auth.enums.Role;
import com.teambiund.bander.auth_server.auth.enums.Status;
import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.repository.SuspendRepository;
import com.teambiund.bander.auth_server.auth.service.suspension.SuspensionManagementService;
import com.teambiund.bander.auth_server.auth.util.generator.key.KeyProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SuspensionManagementServiceImpl implements SuspensionManagementService {

  private final SuspendRepository suspendRepository;
  private final AuthRepository authRepository;
  private final KeyProvider keyProvider;

  public void release(String userId) throws CustomException {
    Auth suspended =
        authRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    if (suspended.getStatus().equals(Status.ACTIVE))
      throw new CustomException(AuthErrorCode.USER_NOT_SUSPENDED);
    suspended.setStatus(Status.ACTIVE);
    authRepository.save(suspended);
  }

  /** 사용자 정지 처리 - Auth 엔티티의 편의 메서드를 사용하여 양방향 연관관계 설정 - Cascade 설정으로 Suspend 엔티티 자동 저장 */
  public void suspend(String userId, String suspendReason, String suspenderUserId, Long suspendDate)
      throws CustomException {
    Auth suspended =
        authRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    Auth suspender =
        authRepository
            .findById(suspenderUserId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
    if (!suspender.getUserRole().equals(Role.ADMIN)) {
      throw new CustomException(AuthErrorCode.NOT_ADMIN);
    }

    if (suspended.getStatus().equals(Status.BLOCKED)) {
      throw new CustomException(AuthErrorCode.USER_ALREADY_BLOCKED);
    }

    Suspend suspend =
        Suspend.builder()
            .id(keyProvider.generateKey())
            .reason(suspendReason)
            .suspendAt(LocalDateTime.now())
            .suspendUntil(LocalDate.now().plusDays(suspendDate))
            .suspenderUserId(suspenderUserId)
            .build();

    // 편의 메서드 사용 - 양방향 연관관계 설정
    suspended.addSuspension(suspend);
    suspended.setStatus(Status.BLOCKED);

    // CascadeType.ALL로 인해 auth만 save하면 suspend도 자동 저장됨
    authRepository.save(suspended);
  }
}
