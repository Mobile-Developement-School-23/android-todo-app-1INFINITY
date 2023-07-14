package ru.mirea.ivashechkinav.todo.core

import kotlinx.coroutines.delay
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AppScope
class OperationRepeatHandler @Inject constructor(
    val repository: TodoItemsRepository
) {
    private val repetitions = AtomicInteger(INITIAL_ATTEMPTS)

    suspend fun <T> retryWithAttempts(
        maxAttempts: Int = DEFAULT_ATTEMPTS,
        block: suspend () -> ResultData<T>
    ): ResultData<T> {
        while (repetitions.get() < maxAttempts) {
            val result = block()
            if (result is ResultData.Failure && exceptionsCatching.any { it.isInstance(result.exception) }) {
                repository.syncItems()
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
        const val DELAY = 0L
    }
}
