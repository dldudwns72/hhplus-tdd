@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.hhplus.tdd.point.integration

import io.hhplus.tdd.point.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(SpringExtension::class)
@SpringBootTest
class PointControllerTest {
    @Autowired
    private lateinit var pointController: PointController

    @MockBean
    private lateinit var pointService: PointService

    private val mockMvc: MockMvc by lazy {
        MockMvcBuilders.standaloneSetup(pointController).build()
    }

    @Test
    fun `특정 유저의 포인트 조회`() {
        // given
        val userId = 1L
        val mockUserPoint = UserPoint(userId, 10_000, System.currentTimeMillis())
        given(pointService.getUserPoint(userId)).willReturn(mockUserPoint)

        // when & then
        mockMvc
            .perform(get("/point/$userId"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(10_000))

        // verify
        verify(pointService).getUserPoint(userId)
    }

    @Test
    fun `특정 유저의 포인트 충전 및 이용 내역 조회`() {
        // given
        val userId = 1L
        val mockHistoryList =
            listOf(
                PointHistory(1, userId, TransactionType.CHARGE, 5000, System.currentTimeMillis()),
                PointHistory(2, userId, TransactionType.USE, 2000, System.currentTimeMillis()),
            )
        given(pointService.getUserPointHistories(userId)).willReturn(mockHistoryList)

        // when & then
        mockMvc
            .perform(get("/point/$userId/histories"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))

        // verify
        verify(pointService).getUserPointHistories(userId)
    }

    @Test
    fun `포인트 충전`() {
        // given
        val userId = 1L
        val amount = 5000L
        val mockUserPoint = UserPoint(userId, 15_000, System.currentTimeMillis())
        given(pointService.charge(userId, amount)).willReturn(mockUserPoint)

        // when & then
        mockMvc
            .perform(
                patch("/point/$userId/charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(amount.toString()),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(15_000))

        // verify
        verify(pointService).charge(userId, amount)
    }

    @Test
    fun `포인트 사용`() {
        // given
        val userId = 1L
        val amount = 2_000L
        val mockUserPoint = UserPoint(userId, 8_000L, System.currentTimeMillis())
        given(pointService.use(userId, amount)).willReturn(mockUserPoint)

        // when & then
        mockMvc
            .perform(
                patch("/point/$userId/use")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(amount.toString()),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(8_000))

        // verify
        verify(pointService).use(userId, amount)
    }
}
