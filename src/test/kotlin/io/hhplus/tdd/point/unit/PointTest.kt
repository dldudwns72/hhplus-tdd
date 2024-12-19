@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.hhplus.tdd.point.unit

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.lock.LockManager
import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.UserPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PointTest {
    @Mock
    private lateinit var pointHistoryRepository: PointHistoryTable

    @Mock
    private lateinit var userPointRepository: UserPointTable

    @InjectMocks
    private lateinit var pointService: PointService

    @Mock
    private lateinit var lockManager: LockManager

    @ParameterizedTest
    @ValueSource(longs = [0, 2_000_001])
    fun `한 번에 충전할 수 있는 포인트는 1 미만 2_000_000 초과일 경우 예외가 발생한다`(amount: Long) {
        val userId = 1L
        val pointId = 1L
        assertThrows<IllegalStateException> {
            PointHistory(pointId, userId, TransactionType.CHARGE, amount, System.currentTimeMillis())
        }
    }

    @ParameterizedTest
    @ValueSource(longs = [1, 2_000_000])
    fun `한 번에 충전할 수 있는 포인트는 1 이상 2_000_000 이하이다`(amount: Long) {
        val userId = 1L
        val pointId = 1L
        val pointHistory = PointHistory(pointId, userId, TransactionType.CHARGE, amount, System.currentTimeMillis())
        assert(pointHistory.amount == amount)
    }

    @Test
    fun `유저 포인트 충전 시 보유 포인트는 최대 10_000_000 초과일 떄 예외가 발생한다`() {
        val userId = 1L
        val userPoint = 9_000_000L
        val amount = 2_000_000L
        val mockUser = UserPoint(userId, userPoint, System.currentTimeMillis())

        assertThrows<IllegalStateException> {
            mockUser.charge(amount)
        }
    }

    @Test
    fun `정상적인 유저 포인트 충전`() {
        // Given
        val userId = 1L
        val currentPoint = 1_000_000L
        val chargeAmount = 2_000_000L

        val mockUser = UserPoint(userId, currentPoint, System.currentTimeMillis())
        val mockUserResult = UserPoint(userId, currentPoint + chargeAmount, System.currentTimeMillis())

        `when`(userPointRepository.selectById(userId)).thenReturn(mockUser)

        `when`(
            userPointRepository.insertOrUpdate(
                userId,
                mockUserResult.point,
            ),
        ).thenReturn(mockUserResult)

        `when`(
            lockManager.getLock(userId) {
                pointService.charge(userId, chargeAmount)
            },
        ).thenReturn(mockUserResult)
    }

    @Test
    fun `잔고가 부족할 경우, 포인트 사용은 실패`() {
        val userId = 1L
        val userPoint = 100L
        val amount = 300L
        val mockUser = UserPoint(userId, userPoint, System.currentTimeMillis())
        assertThrows<IllegalStateException> {
            mockUser.use(amount)
        }
    }

    @Test
    fun `정상적인 유저 포인트 사용`() {
        val userId = 1L
        val userPoint = 3_000_000L
        val mockUser = UserPoint(userId, userPoint, System.currentTimeMillis())
        `when`(userPointRepository.selectById(userId)).thenReturn(mockUser)
        val amount = 1_000_000L

        val mockUserResult = mockUser.use(amount)
        `when`(
            userPointRepository.insertOrUpdate(mockUserResult.id, mockUserResult.point),
        ).thenReturn(mockUserResult)

        `when`(
            lockManager.getLock(userId) {
                pointService.use(userId, amount)
            },
        ).thenReturn(
            UserPoint(
                userId,
                userPoint - amount,
                System.currentTimeMillis(),
            ),
        )

        verify(userPointRepository).selectById(userId)
        verify(userPointRepository).insertOrUpdate(userId, mockUser.point - amount)
    }

    @Test
    fun `히스토리 추가`() {
        val pointHistories = mutableListOf<PointHistory>()
        val userId = 1L
        val historySize = 100
        for (i in 1..historySize) {
            pointHistories.add(
                PointHistory(
                    i.toLong(),
                    userId,
                    TransactionType.CHARGE,
                    i * 10_000L,
                    System.currentTimeMillis(),
                ),
            )
        }
        `when`(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories)
        val result = pointService.getUserPointHistories(userId)
        assert(result.size == historySize)
        verify(pointHistoryRepository).selectAllByUserId(userId)
    }
}
