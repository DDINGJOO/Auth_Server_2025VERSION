package com.teambiund.bander.auth_server.util.vailidator;

import com.teambiund.bander.auth_server.dto.request.ConsentRequest;
import com.teambiund.bander.auth_server.exceptions.CustomException;

import java.util.List;

public interface Validator
{
    void emailValid(java.lang.String email) throws CustomException;

    void passwordValid(java.lang.String password) throws CustomException;

    void passConfirmValid(java.lang.String password, java.lang.String passConfirm) throws CustomException;

    void requiredValid(List<String> value) throws CustomException;

    boolean validateConsentList(List<ConsentRequest> value) throws CustomException;

    boolean validatePhoneNumber(java.lang.String phoneNumber) throws CustomException;


}
