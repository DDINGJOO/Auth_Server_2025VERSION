package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.Suspend;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.SuspendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class SuspendedService {
    private SuspendRepository suspendRepository;
    private AuthRepository authRepository;

    public void suspend(String userId, String suspendReason, String suspenderUserId) throws CustomException {
        Auth suspended = authRepository.findById(userId).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Auth suspender = authRepository.findById(suspenderUserId).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        if(!suspender.getUserRole().getRole().equals(Role.ADMIN))
        {
            throw new CustomException(ErrorCode.NOT_ADMIN);
        }

        if(suspended.getStatus().equals(Status.BLOCKED))
        {
            throw new CustomException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        suspendRepository.save(
                Suspend.builder()
                        .reason(suspendReason)
                        .suspendAt(LocalDateTime.now())
                        .suspenderUserId(suspenderUserId)
                        .suspendedUserId(userId)
                        .build()
        );
    }
}
