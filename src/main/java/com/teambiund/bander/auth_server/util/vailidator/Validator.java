package com.teambiund.bander.auth_server.util.vailidator;

import com.teambiund.bander.auth_server.exceptions.CustomException;

public interface Validator
{
    void emailValid(String email) throws CustomException;
    void passwordValid(String password) throws CustomException;
    void passConfirmValid(String password, String passConfirm) throws CustomException;
}
