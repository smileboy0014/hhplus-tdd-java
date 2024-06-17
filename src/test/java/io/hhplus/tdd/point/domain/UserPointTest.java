package io.hhplus.tdd.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPointTest {

    @DisplayName("처음 생성된 유저의 포인트는 0이다.")
    @Test
    void init() {
        //given
        long userId = 1L;

        // when
        UserPoint result = UserPoint.empty(userId);

        //then
        assertThat(result.point()).isEqualTo(0);
    }
}