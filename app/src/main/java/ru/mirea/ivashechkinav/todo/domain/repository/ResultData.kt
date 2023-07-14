package ru.mirea.ivashechkinav.todo.domain.repository

sealed class ResultData<out T> {
    data class Success<out T>(val value: T?) : ResultData<T>()
    data class Failure(val exception: Exception) : ResultData<Nothing>()
}
