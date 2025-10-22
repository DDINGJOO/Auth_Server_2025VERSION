package com.teambiund.bander.auth_server.auth.service.signup;

import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;

public interface SignupService {
  Auth signup(SignupRequest request);

  Auth signupFromOtherProvider(String email, Provider provider);
}
