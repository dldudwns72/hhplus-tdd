package io.hhplus.tdd.point

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    companion object {
        const val USER_MAX_POINT = 10_000_000
        const val USER_MIN_POINT = 0
    }

    fun charge(amount: Long): UserPoint {
        if (amount + point > USER_MAX_POINT) {
            throw IllegalStateException("포인트는 $USER_MAX_POINT 를 초과할 수 없습니다.")
        }
        return UserPoint(id, amount + point, updateMillis)
    }

    fun use(amount: Long): UserPoint {
        if (point - amount < USER_MIN_POINT) {
            throw IllegalStateException("포인트는 $USER_MIN_POINT 이하가 될 수 없습니다.")
        }
        return UserPoint(id, point - amount, updateMillis)
    }
}
