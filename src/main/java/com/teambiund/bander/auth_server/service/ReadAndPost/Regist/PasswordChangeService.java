package com.teambiund.bander.auth_server.service.ReadAndPost.Regist;

import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.entity.History;
import com.teambiund.bander.auth_server.exceptions.CustomException;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.repository.HistoryRepository;
import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import com.teambiund.bander.auth_server.util.vailidator.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordChangeService
{
    private final AuthRepository authRepository;
    private final HistoryRepository historyRepository;
    private final KeyProvider keyProvider;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(String email, String newPassword, String passConfirm) throws CustomException {
        Auth auth = authRepository.findByEmail(email).orElseThrow(()
                -> new RuntimeException("User not found"));
        validator.passwordValid(newPassword);
        validator.passConfirmValid(newPassword,passConfirm);
        changePassword(auth, newPassword);

    }

    private void changePassword(Auth auth, String newPassword) {
        auth.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(auth);

        historyRepository.save(History.builder()
                .id(keyProvider.generateKey())
                .afterColumnValue(auth.getPassword())
                .updatedColumn("password")
                .updatedAt(LocalDateTime.now())
                .build());

    }
}
