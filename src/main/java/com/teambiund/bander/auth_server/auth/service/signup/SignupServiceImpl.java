package com.teambiund.bander.auth_server.auth.service.signup;

import com.teambiund.bander.auth_server.auth.dto.request.SignupRequest;
import com.teambiund.bander.auth_server.auth.entity.Auth;
import com.teambiund.bander.auth_server.auth.enums.Provider;
import com.teambiund.bander.auth_server.auth.event.events.CreatedUserEvent;
import com.teambiund.bander.auth_server.auth.event.publish.CreateProfileRequestEventPub;
import com.teambiund.bander.auth_server.auth.service.consent.ConsentManagementService;
import com.teambiund.bander.auth_server.auth.service.update.EmailConfirm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class SignupServiceImpl implements SignupService {
  private final SignupStoreService signupStoreService;
  private final ConsentManagementService consentService;
  private final CreateProfileRequestEventPub publishEvent;
  private final EmailConfirm emailConfirm;

  @Override
  public Auth signup(SignupRequest request) {
    emailConfirm.checkedConfirmedEmail(request.getEmail());

    var auth = signupStoreService.signup(request.getEmail(), request.getPassword());
    publishEvent.createProfileRequestPub(
        new CreatedUserEvent(auth.getId(), auth.getProvider().toString()));

    consentService.saveConsent(auth, request.getConsentReqs());
    return auth;
  }

  @Override
  public Auth signupFromOtherProvider(String email, Provider provider) {
    var auth = signupStoreService.signupFromOtherProvider(email, provider);
    publishEvent.createProfileRequestPub(
        new CreatedUserEvent(auth.getId(), auth.getProvider().toString()));
    return auth;
  }
}
