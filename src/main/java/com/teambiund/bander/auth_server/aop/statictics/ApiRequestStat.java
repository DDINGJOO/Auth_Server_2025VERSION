package com.teambiund.bander.auth_server.aop.statictics;


import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
