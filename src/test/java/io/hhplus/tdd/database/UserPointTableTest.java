package io.hhplus.tdd.database;

import io.hhplus.tdd.point.domain.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserPointTableTest {

    @Autowired
    private UserPointTable userPointTable;

    @DisplayName("유저에 대한 포인트를 조회한다.")
    @Test
    void selectById() {
        //given
        long userId = 1;

        //when
        UserPoint result = userPointTable.selectById(userId);

        // then
        Assertions.assertThat(result.point()).isEqualTo(0);
    }

    @DisplayName("유저에 대한 포인트를 충전한다.")
    @Test
    void insertOrUpdate() {
        //given
        long userId = 1;
        long amount = 1000;

        //when
        UserPoint result = userPointTable.insertOrUpdate(userId, amount);

        //then
        Assertions.assertThat(result.point()).isEqualTo(amount);
    }
}