package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;


import com.teambiund.bander.auth_server.entity.Withdraw;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.WithdrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawService {
    private final WithdrawRepository withdrawRepository;
    private final AuthRepository authRepository;

    @Transactional
    public void withdraw(String userId, String withdrawReason) throws CustomException {
        var auth = authRepository.findById(userId).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        withdrawRepository.save(
                Withdraw.builder()
                        .id(auth.getId())
                        .withdrawReason(withdrawReason)
                        .withdrawAt(LocalDateTime.now())
                        .build()
        );
        auth.setStatus(Status.DELETED);
        authRepository.save(auth);
    }


    public void withdrawRetraction(String email) throws CustomException {
        var auth = authRepository.findByEmail(email).orElseThrow(
                () ->  new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        var withdraw = withdrawRepository.findById(auth.getId()).orElseThrow(
                () ->  new CustomException(ErrorCode.WITHDRAW_NOT_FOUND)
        );


        auth.setStatus(Status.ACTIVE);
        authRepository.save(auth);
        withdrawRepository.delete(withdraw);

        withdrawRepository.deleteById(auth.getId());
    }
}
