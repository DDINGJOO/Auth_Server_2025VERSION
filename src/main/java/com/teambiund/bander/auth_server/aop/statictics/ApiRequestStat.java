package com.teambiund.bander.auth_server.aop.statictics;


import com.teambiund.bander.auth_server.util.generator.key.KeyProvider;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiRequestStat {

    private final RedisTemplate<String, String> redisTemplate;
    private final KeyProvider keyProvider;

    public void increment(String apiName) {
        String key = apiName + "-" + LocalDate.now();
        Long offset = keyProvider.generateLongKey() % 10000L;
        redisTemplate.opsForValue().setBit(key, offset, true);
    }


}
