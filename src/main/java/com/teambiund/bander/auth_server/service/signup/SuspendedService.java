package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.SuspendRepository;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class SuspendedService {

    private final SuspendRepository suspendRepository;
    private final AuthRepository authRepository;
    private final KeyProvider keyProvider;

    public void release(String userId) throws CustomException {
        Auth suspended = authRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if (suspended.getStatus().equals(Status.ACTIVE))
            throw new CustomException(ErrorCode.USER_NOT_SUSPENDED);
        suspended.setStatus(Status.ACTIVE);
        authRepository.save(suspended);
    }

    public void suspend(String userId, String suspendReason, String suspenderUserId, Long suspendDate) throws CustomException {
        Auth suspended = authRepository.findById(userId).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Auth suspender = authRepository.findById(suspenderUserId).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if (!suspender.getUserRole().equals(Role.ADMIN))
        {
            throw new CustomException(ErrorCode.NOT_ADMIN);
        }

        if(suspended.getStatus().equals(Status.BLOCKED))
        {
            throw new CustomException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        suspendRepository.save(
                Suspend.builder()
                        .id(keyProvider.generateKey())
                        .reason(suspendReason)
                        .suspendAt(LocalDateTime.now())
                        .suspendUntil(LocalDate.now().plusDays(suspendDate))
                        .suspenderUserId(suspenderUserId)
                        .suspendedUserId(userId)
                        .build()
        );
        suspended.setStatus(Status.BLOCKED);
        authRepository.save(suspended);
    }


}
