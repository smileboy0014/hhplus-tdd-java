package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("유저의 포인트를 조회한다")
    @Test
    void point() throws Exception {
        //given
        long userId = 1;
        UserPoint result = new UserPoint(userId, 1000, System.currentTimeMillis());
        when(pointService.getPoint(userId)).thenReturn(result);

        //when //then
        mockMvc.perform(get("/point/%s".formatted(userId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(1000));
    }

    @DisplayName("유저의 포인트를 충전한다.")
    @Test
    void charge() throws Exception {
        //given
        long userId = 1;
        long amount = 1000;
        UserPoint result = new UserPoint(userId, amount, System.currentTimeMillis());
        when(pointService.charge(userId, amount)).thenReturn(result);

        //when //then
        mockMvc.perform(patch("/point/%s/charge".formatted(userId))
                        .content(objectMapper.writeValueAsString(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(amount));

    }


    @DisplayName("유저의 포인트를 사용한다.")
    @Test
    void use() throws Exception {
        //given
        long userId = 1;
        long amount = 1000;
        long useAmount = 0;

        UserPoint result = new UserPoint(userId, useAmount, System.currentTimeMillis());
        when(pointService.use(userId, amount)).thenReturn(result);

        //when //then
        mockMvc.perform(patch("/point/%s/use".formatted(userId))
                .content(objectMapper.writeValueAsString(amount))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(useAmount));


    }
}