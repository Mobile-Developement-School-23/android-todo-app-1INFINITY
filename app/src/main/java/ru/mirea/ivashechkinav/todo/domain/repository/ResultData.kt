package ru.mirea.ivashechkinav.todo.domain.repository

sealed class ResultData<out T> {
    data class Success<out T>(val value: T?) : ResultData<T>()

    data class Failure<out T>(val message : String) : ResultData<T>()

    data class Loading<out T>(val value: T? = null) : ResultData<T>()

    companion object {

        fun <T> success(value: T? = null): ResultData<T> = Success(value)

        fun <T> failure(errorMsg : String ): ResultData<T> = Failure(errorMsg)

        fun <T> loading(value: T? = null): ResultData<T> = Loading(value)
    }
}
