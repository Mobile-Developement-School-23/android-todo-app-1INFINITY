package ru.mirea.ivashechkinav.todo.core

import ru.mirea.ivashechkinav.todo.domain.repository.ResultData

suspend fun <T> retryWithAttempts(
    attempts: Int = 3,
    block: suspend () -> ResultData<T>
): ResultData<T> {
    var remainingAttempts = attempts
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            if(e is UnauthorizedException)
                return ResultData.Failure(e)
            remainingAttempts--
            if(remainingAttempts == 0)
                return ResultData.Failure(e)
        }
    }
}