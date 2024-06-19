package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;
import static io.hhplus.tdd.point.exception.ErrorCode.NOT_ENOUGH_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 모든 테스트의 유저 id를 다르게 함으로써 stub 을 사용하지 않아도 됨 + tearDown 도 필요 없음
@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @DisplayName("유저 id를 받아, 해당 유저의 포인트를 조회한다")
    @Test
    void getPoint() {
        //given
        long userId = 1;

        // when
        UserPoint result = pointService.getPoint(userId);

        //then
        assertThat(result.point()).isEqualTo(0);
    }

    @DisplayName("받은 포인트만큼 포인트를 충전한다.")
    @Test
    void charge() {
        //given
        long userId = 2;
        long chargeAmount = 1000;

        //when
        UserPoint result = pointService.charge(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(chargeAmount);
    }

    @DisplayName("0 미만의 포인트를 충전하려고하면 예외를 반환한다.")
    @Test
    void chargeInvalidPoint() {
        //given
        long userId = 3;
        long amount = -1000;

        //when //then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }

    @DisplayName("동시에 같은 유저가 100 포인트를 5번 충전하면 500이 되어야 한다.")
    @Test
    void chargeWhenConcurrencyEnv() throws InterruptedException {
        //given
        long userId = 4;
        int numThreads = 5;
        long chargeAmount = 100;
        long resultAmount = 500;

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
        long userId = 5;
        long initAmount = 1000;
        long useAmount = 100;
        long resultAmount = 900;

        pointService.charge(userId, initAmount);

        //when
        UserPoint result = pointService.use(userId, useAmount);

        //then
        assertThat(result.point()).isEqualTo(resultAmount);
    }

    @DisplayName("0 미만의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useInvalidPoint() {
        //given
        long userId = 6;
        long amount = -1000;

        //when //then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }

    @DisplayName("가지고 있는 포인트 이상의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useOverPoint() {
        //given
        long userId = 7;
        long useAmount = 2000;

        //when //then
        assertThatThrownBy(() -> pointService.use(userId, useAmount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(NOT_ENOUGH_POINT);

    }

    @DisplayName("동시에 500포인트를 가진 같은 유저가 100 포인트를 5번 사용하면 0 포인트가 된다.")
    @Test
    void useWhenConcurrencyEnv() throws InterruptedException {
        //given
        long userId = 8;
        int numThreads = 5;
        long useAmount = 100;
        long remainAmount = 500;
        long resultAmount = 0;

        pointService.charge(userId, remainAmount);

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
        long userId = 9;
        long initAmount = 1000;

        pointService.charge(userId, initAmount);

        //when
        List<PointHistory> result = pointService.getHistory(userId);

        //then
        assertThat(result.size()).isEqualTo(1);
    }
}