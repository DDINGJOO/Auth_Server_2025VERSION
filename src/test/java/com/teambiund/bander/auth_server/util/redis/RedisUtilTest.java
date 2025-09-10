package com.teambiund.bander.auth_server.util.redis;

import com.teambiund.bander.auth_server.AuthServerApplication;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AuthServerApplication.class)
@RequiredArgsConstructor
public class RedisUtilTest {
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisUtil redisUtil;

    @Test
    void debugSerializers() {
        System.out.println("keySerializer = " + redisTemplate.getKeySerializer());
        System.out.println("valueSerializer = " + redisTemplate.getValueSerializer());
    }


    @Test
    @DisplayName("코드 발급 정상 확인 ")
    public void generateCodeTest() {
        String code = redisUtil.generateCode("test");
        System.out.println(code);
        assert code.length() == 6;
    }

    @Test
    @DisplayName("여러 코드의 동시 생성시 유일코드 보장")
    public void generateCode_multiTest() {
        String code1 = redisUtil.generateCode("test");
        String code2 = redisUtil.generateCode("test");
        String code3 = redisUtil.generateCode("test");
        System.out.println("code1 = " + code1);
        System.out.println("code2 = " + code2);
        System.out.println("code3 = " + code3);
        assert code1.equals(code2) == false;
        assert code1.equals(code3) == false;
        assert code2.equals(code3) == false;
        assert code1.length() == 6;
        assert code2.length() == 6;
        assert code3.length() == 6;
    }


    @Test
    @DisplayName("코드 인증 정상 확인 ")
    public void checkCodeTest() {
        // 코드 발급
        String code = redisUtil.generateCode("test");
        System.out.println("generated code = " + code);

        // 한 번만 조회 (checkCode는 키를 삭제함)
        String userId = redisUtil.checkCode(code);
        System.out.println("checked userId = " + userId);

        // JUnit Assertions 사용
        assertNotNull(userId, "userId는 null이면 안됩니다");
        assertEquals("test", userId, "조회된 userId가 예상값과 달라요");

        // 이미 삭제됐으므로 다시 조회하면 null
        assertNull(redisUtil.checkCode(code), "이미 사용된 코드는 null 이어야 합니다");
    }

}
