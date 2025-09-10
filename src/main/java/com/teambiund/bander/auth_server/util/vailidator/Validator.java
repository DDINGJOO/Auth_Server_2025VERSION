package com.teambiund.bander.auth_server.util.vailidator;

import com.teambiund.bander.auth_server.enums.ConsentType;
import com.teambiund.bander.auth_server.exceptions.CustomException;

import java.util.List;

public interface Validator
{
    void emailValid(String email) throws CustomException;
    void passwordValid(String password) throws CustomException;
    void passConfirmValid(String password, String passConfirm) throws CustomException;

    void requiredValid(List<ConsentType> value) throws CustomException;
}
