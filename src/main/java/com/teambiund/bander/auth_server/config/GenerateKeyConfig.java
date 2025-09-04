package com.teambiund.bander.auth_server.config;


import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.key_gerneratre.impl.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenerateKeyConfig {

    @Bean
    public KeyProvider keyGenerator() {
        return new Snowflake();
    }
}
