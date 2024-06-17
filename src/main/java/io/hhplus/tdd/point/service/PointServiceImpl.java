package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.exception.PointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hhplus.tdd.point.exception.ErrorCode.INVALID_CHARGE_POINT;
import static io.hhplus.tdd.point.exception.ErrorCode.NOT_ENOUGH_POINT;

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
        if (!validPoint(amount)) {
            throw new PointException(INVALID_CHARGE_POINT, "0보다 작은 포인트는 충전되지 않습니다.");
        }
        UserPoint curUser = userPointTable.selectById(id);

        return userPointTable.insertOrUpdate(id, curUser.point() + amount);
    }

    @Override
    public UserPoint use(long id, long amount) {

        // 포인트의 유효성 체크
        if (!validPoint(amount)) {
            throw new PointException(INVALID_CHARGE_POINT, "0보다 작은 포인트는 사용할 수 없습니다.");
        }

        UserPoint userPoint = userPointTable.selectById(id);

        // 포인트가 부족하지 않은지 체크
        if (!isPossibleUse(amount, userPoint.point())) {
            throw new PointException(NOT_ENOUGH_POINT, "포인트가 부족합니다.");
        }

        return userPointTable.insertOrUpdate(id, userPoint.point() - amount);
    }

    private boolean validPoint(long amount) {
        return amount >= 0;
    }

    private boolean isPossibleUse(long amount, long userPoint) {
        return amount <= userPoint;
    }
}
