package com.teambiund.bander.auth_server.auth.service.withdrawal.impl;

import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.exception.ErrorCode.AuthErrorCode;
import com.teambiund.bander.auth_server.auth.repository.AuthRepository;
import com.teambiund.bander.auth_server.auth.service.withdrawal.WithdrawalManagementService;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithdrawalManagementServiceImpl implements WithdrawalManagementService {
    private final AuthRepository authRepository;
    private final CipherStrategy emailCipher;

    public WithdrawalManagementServiceImpl(
            AuthRepository authRepository,
            @Qualifier("aesCipherStrategy") CipherStrategy emailCipher
    ) {
        this.authRepository = authRepository;
        this.emailCipher = emailCipher;
    }

    /**
     * 회원 탈퇴 처리
     * - Auth 엔티티의 편의 메서드를 사용하여 탈퇴 처리
     * - Cascade 설정으로 Withdraw 엔티티 자동 저장
     */
    public void withdraw(String userId, String withdrawReason) throws CustomException {
    var auth =
        authRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 편의 메서드 사용 - Withdraw 엔티티 생성 및 양방향 연관관계 설정
        auth.markAsDeleted(withdrawReason);

        // CascadeType.ALL로 인해 auth만 save하면 withdraw도 자동 저장됨
        authRepository.save(auth);
    }


    /**
     * 회원 탈퇴 철회
     * - Auth 엔티티의 편의 메서드를 사용하여 탈퇴 철회
     * - orphanRemoval=true 설정으로 Withdraw 엔티티 자동 삭제
     */
    public void withdrawRetraction(String email) throws CustomException {
        String encryptedEmail = emailCipher.encrypt(email);
    var auth =
        authRepository
            .findByEmail(encryptedEmail)
            .or(() -> authRepository.findByEmail(email))
            .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        if (auth.getWithdraw() == null) {
      throw new CustomException(AuthErrorCode.WITHDRAW_NOT_FOUND);
        }

        // 편의 메서드 사용 - 상태 변경 및 withdraw 연관관계 제거
        auth.cancelWithdrawal();

        // orphanRemoval=true로 인해 withdraw 참조 제거 시 자동 삭제됨
        authRepository.save(auth);
    }
}
