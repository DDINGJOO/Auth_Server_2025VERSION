package com.teambiund.bander.auth_server.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // application.yml(또는 properties)에 설정된 값을 주입받습니다.
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 서버와의 연결을 생성하는 ConnectionFactory Bean 입니다.
     * Lettuce는 Netty 기반의 고성능 Redis 클라이언트입니다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Redis 작업을 편리하게 수행할 수 있도록 돕는 RedisTemplate Bean 입니다.
     * 여기서는 Key와 Value 모두 String을 사용하는 일반적인 시나리오를 가정하여 StringRedisTemplate을 등록합니다.
     */
    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory());


        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}

