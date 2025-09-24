package com.teambiund.bander.auth_server.service.signup;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Provider;

public interface SignupService {
    Auth signup(String email, String password, String passConfirm);

    Auth signupFromOtherProvider(String email, Provider provider);
}
