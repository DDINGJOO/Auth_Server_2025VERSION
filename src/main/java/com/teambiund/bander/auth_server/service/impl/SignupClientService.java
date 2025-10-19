package com.teambiund.bander.auth_server.service.impl;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.SignupClientInterface;
import com.teambiund.bander.auth_server.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.service.signup.SignupStoreService;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import com.teambiund.bander.auth_server.service.withdrawal.WithdrawalManagementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupClientService implements SignupClientInterface {
    private final SignupStoreService signupStoreService;
    private final UpdateService updateService;
    private final WithdrawalManagementService withdrawService;
    private final ConsentManagementService consentService;

    @Override
    @Transactional
    public void signup(SignupRequest request) throws CustomException {
        Auth auth = signupStoreService.signup(request.getEmail(), request.getPassword());
        consentService.saveConsent(auth, request.getConsentReqs());

    }

    @Override
    @Transactional
    public void passwordChange(String email, String newPassword, String newPasswordConfirm) throws CustomException {
        updateService.changePassword(email, newPassword, newPasswordConfirm);

    }

    @Override
    @Transactional
    public void withdraw(String userId, String withdrawReason) throws CustomException {
        withdrawService.withdraw(userId, withdrawReason);

    }

    @Override
    public void changeConsent(String userId, List<ConsentRequest> req) throws CustomException {
        consentService.changeConsent(userId, req);
    }
}
