package com.teambiund.bander.auth_server.config;

import com.teambiund.bander.auth_server.util.password_encoder.BCryptUtil;
import com.teambiund.bander.auth_server.util.password_encoder.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptUtil();
    }
}
