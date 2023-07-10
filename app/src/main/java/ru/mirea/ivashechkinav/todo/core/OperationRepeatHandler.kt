package ru.mirea.ivashechkinav.todo.core

import kotlinx.coroutines.delay
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import java.util.concurrent.atomic.AtomicInteger

class OperationRepeatHandler(
    val syncAction: suspend () -> ResultData<Unit>
) {
    private val repetitions = AtomicInteger(INITIAL_ATTEMPTS)

    suspend fun <T> retryWithAttempts(
        maxAttempts: Int = DEFAULT_ATTEMPTS,
        block: suspend () -> ResultData<T>
    ): ResultData<T> {
        while (repetitions.get() < maxAttempts) {
            val result = block()
            if (result is ResultData.Failure && exceptionsCatching.any { it.isInstance(result.exception) }) {
                syncAction()
            } else {
                repetitions.set(INITIAL_ATTEMPTS)
                return result
            }
            repetitions.incrementAndGet()
            delay(DELAY)
        }
        return ResultData.Failure(UnableToPerformOperation())
    }

    companion object {
        const val INITIAL_ATTEMPTS = 0
        const val DEFAULT_ATTEMPTS = 3
        val exceptionsCatching = listOf(
            OutOfSyncDataException::class,
            ServerSideException::class
        )
        const val DELAY = 100L
    }
}