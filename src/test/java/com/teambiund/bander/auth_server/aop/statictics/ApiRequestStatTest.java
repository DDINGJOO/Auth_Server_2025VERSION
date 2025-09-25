package com.teambiund.bander.auth_server.aop.statictics;

import com.teambiund.bander.auth_server.util.generator.key_gerneratre.KeyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApiRequestStatTest {

    @Test
    @DisplayName("increment 호출 시 Redis 비트맵에 setBit가 호출된다")
    void increment_sets_bit_in_redis() {
        // given
        @SuppressWarnings("unchecked")
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        KeyProvider keyProvider = mock(KeyProvider.class);
        when(keyProvider.generateLongKey()).thenReturn(42L);

        ApiRequestStat stat = new ApiRequestStat(redisTemplate, keyProvider);

        String apiName = "GET /sample";
        String expectedKey = apiName + "-" + LocalDate.now();

        // when
        stat.increment(apiName);

        // then
        verify(valueOps).setBit(eq(expectedKey), eq(42L), eq(true));
    }


}
