package com.teambiund.bander.auth_server.factory;


import com.teambiund.bander.auth_server.entity.Auth;
import com.teambiund.bander.auth_server.enums.Status;
import com.teambiund.bander.auth_server.util.key_gerneratre.KeyProvider;
import com.teambiund.bander.auth_server.util.key_gerneratre.impl.Snowflake;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = com.teambiund.bander.auth_server.AuthServerApplication.class)
public class DataInitializer {

    static final int BULK_INSERT_SIZE = 200;
    static final int EXECUTE_COUNT = 100;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    KeyProvider keyProvider = new Snowflake();
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    @Test
    void initialize() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < EXECUTE_COUNT; i++) {
            executorService.submit(() -> {
                insert();
                latch.countDown();
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < BULK_INSERT_SIZE; i++) {
                Auth article = Auth.builder()
                        .createdAt(LocalDateTime.now())
                        .email("user" + i + "@example.com")
                        .id(keyProvider.generateKey())
                        .password("password" + i)
                        .status(Status.ACTIVE)
                        .build();
                entityManager.persist(article);
            }
        });
    }


}
