package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;

import java.util.List;

public interface PointService {

    UserPoint getPoint(long id);

    UserPoint charge(long id, long amount);

    UserPoint use(long id, long amount);

    List<PointHistory> getHistory(long id);
}
