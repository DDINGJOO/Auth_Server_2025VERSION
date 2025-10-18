package com.teambiund.bander.auth_server.config;


import com.teambiund.bander.auth_server.util.validator.impl.ValidatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public ValidatorImpl validator() {
        return new ValidatorImpl();
    }


}
