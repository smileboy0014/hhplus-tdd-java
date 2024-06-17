package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final UserPointTable userPointTable;

    public void cleanUserPointTable(long userId) {
        userPointTable.insertOrUpdate(userId, 0);
    }
}
