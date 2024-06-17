package io.hhplus.tdd.point.service;


import io.hhplus.tdd.point.domain.UserPoint;

public interface PointService {

    UserPoint getPoint(long id);

    UserPoint charge(long id, long amount);
}
