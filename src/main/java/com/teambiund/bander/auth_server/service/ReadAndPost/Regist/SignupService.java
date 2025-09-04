package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;


import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.UserRole;
import com.teambiund.bander.auth_server.enums.Provider;
import com.teambiund.bander.auth_server.enums.Role;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.exceptions.ErrorCode.ErrorCode;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.UserRoleRepository;
import com.teambiund.bander.auth_server.util.BCryptUtil;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService {
    private final AuthRepository authRepository;
    private final UserRoleRepository userRoleRepository;
    private final Validator validator;
    private final KeyProvider keyProvider;

    public void signup(String email, String password, String passConfirm) throws CustomException {
        validator(email, password, passConfirm);
        if (authRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserRole role = userRoleRepository.findByRole(Role.GUEST);

        Auth auth = Auth.builder()
                .id(keyProvider.generateKey())
                .email(email)
                .password(BCryptUtil.hash(password))
                .provider(Provider.SYSTEM)
                .createdAt(LocalDateTime.now())
                .status(Status.UNCONFIRMED)
                .userRole(role)
                .build();

        authRepository.save(auth);
    }

    // validator
    private void validator(String email, String password, String passConfirm) throws CustomException {
        validator.emailValid(email);
        validator.passwordValid(password);
        validator.passConfirmValid(password, passConfirm);
    }
}
