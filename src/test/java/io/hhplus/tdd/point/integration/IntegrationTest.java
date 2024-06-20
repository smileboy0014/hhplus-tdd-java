package io.hhplus.tdd.point.integration;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.hhplus.tdd.point.enums.TransactionType.CHARGE;
import static io.hhplus.tdd.point.enums.TransactionType.USE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;



    @DisplayName("유저 id를 받아, 해당 유저의 포인트를 조회한다")
    @Test
    void getPoint() {
        //given
        long userId = 1;
        long amount = 1000;

        userPointRepository.insertOrUpdate(userId, amount);

        // when
        UserPoint result = pointService.getPoint(userId);

        //then
        assertThat(result.point()).isEqualTo(amount);
    }

    @DisplayName("기존 1000 포인트에 1000포인트를 충전하면 2000 포인트가 된다.")
    @Test
    void charge() {
        //given
        long userId = 2;
        long initAmount = 1000;
        long chargeAmount = 1000;
        long resultAmount = 2000;

        userPointRepository.insertOrUpdate(userId, initAmount);

        //when
        UserPoint result = pointService.charge(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(resultAmount);
    }


    @DisplayName("5000 포인트를 가진 유저가 동시에 같은 유저가 100 포인트를 5번 충전하면 5500이 되어야 한다.")
    @Test
    void chargeWhenConcurrencyEnv() throws InterruptedException {
        //given
        long userId = 3;
        int numThreads = 5;
        long initAmount = 5000;
        long chargeAmount = 100;
        long resultAmount = 5500;

        userPointRepository.insertOrUpdate(userId, initAmount);

        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        //when
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    pointService.charge(userId, chargeAmount);
                    successCount.getAndIncrement();
                } catch (RuntimeException e) {
                    failCount.getAndIncrement();
                } finally {
                    doneSignal.countDown();
                }
            });
        }
        doneSignal.await();
        executorService.shutdown();

        UserPoint result = pointService.getPoint(userId);
        //then
        assertThat(result.point()).isEqualTo(resultAmount);
        assertThat(successCount.get()).isEqualTo(numThreads);
    }

    @DisplayName("사용하는 포인트만큼 차감이 된다.")
    @Test
    void use() {
        //given
        long userId = 4;
        long initAmount = 1000;
        long useAmount = 100;
        long resultAmount = 900;

        userPointRepository.insertOrUpdate(userId, initAmount);

        //when
        UserPoint result = pointService.use(userId, useAmount);

        //then
        assertThat(result.point()).isEqualTo(resultAmount);
    }

    @DisplayName("동시에 500포인트를 가진 같은 유저가 100 포인트를 5번 사용하면 0 포인트가 된다.")
    @Test
    void useWhenConcurrencyEnv() throws InterruptedException {
        //given
        long userId = 5;
        int numThreads = 5;
        long useAmount = 100;
        long remainAmount = 500;
        long resultAmount = 0;

        userPointRepository.insertOrUpdate(userId, remainAmount);

        CountDownLatch doneSignal = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        //when
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    pointService.use(userId, useAmount);
                    successCount.getAndIncrement();
                } catch (RuntimeException e) {
                    failCount.getAndIncrement();
                } finally {
                    doneSignal.countDown();
                }
            });
        }
        doneSignal.await();
        executorService.shutdown();

        UserPoint result = pointService.getPoint(userId);
        //then
        assertThat(result.point()).isEqualTo(resultAmount);
        assertThat(successCount.get()).isEqualTo(numThreads);
    }

    @DisplayName("포인트 사용 내역을 조회한다.")
    @Test
    void history() {
        //given
        long userId = 6;
        long amount1 = 1000;
        long amount2 = 500;
        long amount3 = 1000;

        pointHistoryRepository.insert(userId, amount1, CHARGE, System.currentTimeMillis());
        pointHistoryRepository.insert(userId, amount2, USE, System.currentTimeMillis());
        pointHistoryRepository.insert(userId, amount3, CHARGE, System.currentTimeMillis());

        //when
        List<PointHistory> result = pointService.getHistory(userId);

        //then
        assertThat(result.size()).isEqualTo(3);
    }
}
