package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;

    @Override
    public UserPoint getPoint(long id) {

        return userPointTable.selectById(id);
    }

    @Override
    public UserPoint charge(long id, long amount) {

        // 포인트의 유효성 체크
        if (amount < 0) {
            throw new PointException(INVALID_CHARGE_POINT, "0보다 작은 포인트는 충전되지 않습니다.");
        }

        return userPointTable.insertOrUpdate(id, amount);
    }
}
