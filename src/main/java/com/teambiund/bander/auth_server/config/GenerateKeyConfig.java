package com.teambiund.bander.auth_server.config;


import com.teambiund.bander.auth_server.event.publish.EmailConfirmRequestEventPub;
import com.teambiund.bander.auth_server.event.publish.EventPublisher;
import com.teambiund.bander.auth_server.service.update.EmailConfirm;
import com.teambiund.bander.auth_server.service.update.impl.EmailConfirmImpl;
import com.teambiund.bander.auth_server.util.generator.generate_code.EmailCodeGenerator;
import com.teambiund.bander.auth_server.util.generator.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.generator.key_gerneratre.impl.Snowflake;
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
    public EmailConfirm emailConfirm(StringRedisTemplate stringRedisTemplate, KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new EmailConfirmImpl(
                new EmailCodeGenerator(stringRedisTemplate),
                new EmailConfirmRequestEventPub(
                        new EventPublisher(kafkaTemplate)
                ));
    }
}
