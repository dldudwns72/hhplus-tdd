package io.hhplus.tdd.lock

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Component
class LockManager(
    private val locks: ConcurrentHashMap<Long, ReentrantLock> = ConcurrentHashMap(),
) {
    fun <T> getLock(
        key: Long,
        action: () -> T,
    ): T {
        val lock = locks.getOrPut(key) { ReentrantLock() }
        lock.lock()
        return try {
            action()
        } finally {
            lock.unlock()
        }
    }
}
