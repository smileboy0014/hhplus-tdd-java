package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.ErrorCode;
import io.hhplus.tdd.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void charge(){
        //given
        long userId = 1;
        long amount = 1000;

        //when
        UserPoint result = pointService.charge(userId, amount);

        //then
        assertThat(result.point()).isEqualTo(amount);
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
                .isEqualTo(ErrorCode.INVALID_CHARGE_POINT);

    }
}