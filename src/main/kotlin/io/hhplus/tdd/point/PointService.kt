package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.lock.LockManager
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointHistoryTable: PointHistoryTable,
    private val userPointTable: UserPointTable,
    private val lockManager: LockManager,
) {
    fun getUserPoint(id: Long): UserPoint = userPointTable.selectById(id)

    fun getUserPointHistories(id: Long): List<PointHistory> = pointHistoryTable.selectAllByUserId(id)

    fun charge(
        id: Long,
        amount: Long,
    ): UserPoint =
        lockManager.getLock(id) {
            val user = userPointTable.selectById(id)
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis())
            val chargedUser = user.charge(amount)
            userPointTable.insertOrUpdate(chargedUser.id, chargedUser.point)
        }

    fun use(
        id: Long,
        amount: Long,
    ): UserPoint =
        lockManager.getLock(id) {
            val user = userPointTable.selectById(id)
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis())
            val pointUsedUser = user.use(amount)
            userPointTable.insertOrUpdate(pointUsedUser.id, pointUsedUser.point)
        }
}
