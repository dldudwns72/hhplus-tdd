package io.hhplus.tdd.point

/**
 * 유저가 충전 및 사용한 포인트 이력
 */
data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
) {
    init {
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw IllegalStateException("충전할 수 있는 포인트는 $MIN_AMOUNT 이상 $MAX_AMOUNT 이하여야 합니다.")
        }
    }

    companion object {
        const val MIN_AMOUNT = 1L
        const val MAX_AMOUNT = 2_000_000L
    }
}

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE,
    USE,
}
