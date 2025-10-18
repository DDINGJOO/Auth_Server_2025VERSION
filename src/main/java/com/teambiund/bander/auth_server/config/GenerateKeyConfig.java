package com.teambiund.bander.auth_server.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambiund.bander.auth_server.event.publish.EmailConfirmRequestEventPub;
import com.teambiund.bander.auth_server.event.publish.EventPublisher;
import com.teambiund.bander.auth_server.repository.AuthRepository;
import com.teambiund.bander.auth_server.service.update.EmailConfirm;
import com.teambiund.bander.auth_server.service.update.impl.EmailConfirmImpl;
import com.teambiund.bander.auth_server.util.generator.generate_code.EmailCodeGenerator;
import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import com.teambiund.bander.auth_server.util.generator.key.impl.Snowflake;
import com.teambiund.bander.auth_server.util.cipher.CipherStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class GenerateKeyConfig {

    @Bean
    public KeyProvider keyGenerator() {
        return new Snowflake();
    }

    @Bean
    public EmailConfirm emailConfirm(StringRedisTemplate stringRedisTemplate, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, AuthRepository authRepository,
                                     @Qualifier("aesCipherStrategy") CipherStrategy emailCipher
    ) {
        return new EmailConfirmImpl(
                new EmailCodeGenerator(authRepository, stringRedisTemplate, emailCipher),
                new EmailConfirmRequestEventPub(
                        new EventPublisher(kafkaTemplate, objectMapper)
                ));
    }
}
