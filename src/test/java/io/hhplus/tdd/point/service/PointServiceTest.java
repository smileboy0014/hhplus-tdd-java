package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private PointService pointService;

    @DisplayName("유저 id를 받아, 해당 유저의 포인트를 조회한다")
    @Test
    void getPoint() {
        //given
        long userId = 1L;

        // when
        UserPoint result = pointService.getPoint(userId);

        //then
        assertThat(result.point()).isEqualTo(0);
    }
}