package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.exception.PointException;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;
import static io.hhplus.tdd.point.exception.ErrorCode.NOT_ENOUGH_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


class PointServiceTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private UserPointRepository userPointRepository;

    private PointService pointService;


    @BeforeEach
    void setUp() {
        // MockitoAnnotations.initMocks(this); deprecated!
        MockitoAnnotations.openMocks(this);
        pointService = new PointServiceImpl(userPointRepository,pointHistoryRepository);
    }

    @DisplayName("유저 id를 받아, 해당 유저의 포인트를 조회한다")
    @Test
    void getPoint() {
        //given
        long userId = 1;
        long initAmount = 1000;

        UserPoint userPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.selectById(userId)).thenReturn(userPoint);
        UserPoint result = pointService.getPoint(userId);

        //then
        assertThat(result.point()).isEqualTo(initAmount);
    }

    @DisplayName("받은 포인트만큼 포인트를 충전한다.")
    @Test
    void charge(){
        //given
        long userId = 1;
        long initAmount = 0;
        long chargeAmount = 1000;

        UserPoint initPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());
        UserPoint chargePoint = new UserPoint(userId, chargeAmount, System.currentTimeMillis());

        //when
        when(userPointRepository.selectById(userId)).thenReturn(initPoint);
        when(userPointRepository.insertOrUpdate(userId,chargeAmount)).thenReturn(chargePoint);
        UserPoint result = pointService.charge(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(chargeAmount);
    }

    @DisplayName("0 미만의 포인트를 충전하려고하면 예외를 반환한다.")
    @Test
    void chargeInvalidPoint(){
        //given
        long userId = 1;
        long amount = -1000;

        //when //then
        assertThatThrownBy(() -> pointService.charge(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }

    @DisplayName("사용하는 포인트만큼 차감이 된다.")
    @Test
    void use(){
        //given
        long userId = 1;
        long initAmount = 1000;
        long useAmount = 100;
        long resultAmount = 900;

        UserPoint initPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());
        UserPoint usePoint = new UserPoint(userId, resultAmount, System.currentTimeMillis());

        //when
        when(userPointRepository.selectById(userId)).thenReturn(initPoint);
        when(userPointRepository.insertOrUpdate(userId,resultAmount)).thenReturn(usePoint);
        UserPoint result = pointService.use(userId, useAmount);

        //then
        assertThat(result.point()).isEqualTo(resultAmount);
    }

    @DisplayName("0 미만의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useInvalidPoint(){
        //given
        long userId = 1;
        long amount = -1000;

        //when //then
        assertThatThrownBy(() -> pointService.use(userId, amount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_CHARGE_POINT);

    }

    @DisplayName("가지고 있는 포인트 이상의 포인트를 사용하려고 하면 예외를 반환한다.")
    @Test
    void useOverPoint(){
        //given
        long userId = 1;
        long initAmount = 1000;
        long useAmount = 2000;
        long resultAmount = -1000;

        UserPoint initPoint = new UserPoint(userId, initAmount, System.currentTimeMillis());
        UserPoint usePoint = new UserPoint(userId, resultAmount, System.currentTimeMillis());

        //when
        when(userPointRepository.selectById(userId)).thenReturn(initPoint);
        when(userPointRepository.insertOrUpdate(userId,resultAmount)).thenReturn(usePoint);

        //then
        assertThatThrownBy(() -> pointService.use(userId, useAmount))
                .isInstanceOf(PointException.class)
                .extracting("errorCode")
                .isEqualTo(NOT_ENOUGH_POINT);

    }

    @DisplayName("포인트 사용 내역을 조회한다.")
    @Test
    void history(){
        //given
        long historyId = 1;
        long userId = 1;
        long initAmount = 1000;

        PointHistory pointHistory = new PointHistory(historyId,userId, initAmount, TransactionType.CHARGE, System.currentTimeMillis());
        List<PointHistory> pointHistories =List.of(pointHistory);

        //when
        when(pointHistoryRepository.insert(userId,initAmount,TransactionType.CHARGE, System.currentTimeMillis())).thenReturn(pointHistory);
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories);
        List<PointHistory> result = pointService.getHistory(userId);
        //then
        assertThat(result.size()).isEqualTo(1);
    }
}