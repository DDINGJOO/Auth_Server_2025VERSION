package com.teambiund.bander.auth_server.service.impl;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.service.SignupClientInterface;
import com.teambiund.bander.auth_server.service.signup.ConsentService;
import com.teambiund.bander.auth_server.service.signup.SignupService;
import com.teambiund.bander.auth_server.service.signup.WithdrawService;
import com.teambiund.bander.auth_server.service.update.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SignupClientService implements SignupClientInterface {
    private final SignupService signupService;
    private final UpdateService updateService;
    private final WithdrawService withdrawService;
    private final ConsentService consentService;

    @Override
    @Transactional
    public void signup(SignupRequest request) throws CustomException {
        Auth auth = signupService.signup(request.getEmail(), request.getPassword(), request.getPasswordConfirm());
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
