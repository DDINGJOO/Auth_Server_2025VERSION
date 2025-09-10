package com.teambiund.bander.auth_server.service;


import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;

import java.util.List;

public interface SignupClientInterface {
    void signup(SignupRequest request) throws CustomException;

    void passwordChange(String email, String newPassword, String newPasswordConfirm) throws CustomException;

    void withdraw(String userId, String withdrawReason) throws CustomException;

    void changeConsent(String userId, List<ConsentRequest> req) throws CustomException;
}
