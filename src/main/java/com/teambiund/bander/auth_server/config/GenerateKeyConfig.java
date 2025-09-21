package com.teambiund.bander.auth_server.config;


import com.teambiund.bander.auth_server.service.update.EmailConfirm;
import com.teambiund.bander.auth_server.service.update.impl.EmailConfirmImpl;
import com.teambiund.bander.auth_server.util.generate_code.EmailCodeGenerator;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.key_gerneratre.impl.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class GenerateKeyConfig {

    @Bean
    public KeyProvider keyGenerator() {
        return new Snowflake();
    }

    @Bean
    public EmailConfirm emailConfirm() {
        return new EmailConfirmImpl(new EmailCodeGenerator(
                new RedisTemplate<String, String>()
        ));
    }
}
