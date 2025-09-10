package com.teambiund.bander.auth_server.util.key_gerneratre.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeTest {

    @Test
    @DisplayName("동시 요청으로 키 생성 시 모든 키가 유니크해야 한다")
    void concurrentGenerationProducesUniqueKeys() throws InterruptedException {
        // Given
        Snowflake generator = new Snowflake();
        int threads = 8;
        int perThread = 2_000; // total 16,000 ids
        int total = threads * perThread;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        // Use a concurrent set backed by ConcurrentHashMap for O(1) inserts
        Set<String> unique = ConcurrentHashMap.newKeySet(total);
        AtomicInteger failures = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        String id = generator.generateKey();
                        if (!unique.add(id)) {
                            failures.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        // Start all workers at once
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertTrue(finished, "작업이 시간 내에 완료되지 않았습니다");
        assertEquals(0, failures.get(), "스레드 중 오류가 발생했습니다");
        assertEquals(total, unique.size(), "중복 키가 발생했습니다");
    }

    @Test
    @DisplayName("여러 Snowflake 인스턴스(서로 다른 nodeId)에서도 충돌이 없어야 한다")
    void multipleInstancesDoNotCollide() throws InterruptedException {
        // Given: 여러 인스턴스를 준비
        int instances = 4;
        int perInstance = 4_000; // total 16,000 again
        int total = instances * perInstance;

        List<Snowflake> gens = new ArrayList<>();
        for (int i = 0; i < instances; i++) {
            gens.add(new Snowflake());
        }

        ExecutorService pool = Executors.newFixedThreadPool(instances);
        Set<String> unique = ConcurrentHashMap.newKeySet(total);
        CountDownLatch done = new CountDownLatch(instances);

        for (Snowflake gen : gens) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perInstance; i++) {
                        unique.add(gen.generateKey());
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertTrue(finished, "작업이 시간 내에 완료되지 않았습니다");
        assertEquals(total, unique.size(), "인스턴스 간 충돌(중복)이 발생했습니다");
    }
}

