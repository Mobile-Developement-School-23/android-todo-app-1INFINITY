package ru.mirea.ivashechkinav.todo.data.retrofit.models

import kotlinx.serialization.Serializable


@Serializable
data class NetworkResponse(
    val status: String?,
    val element: NWTodoItem,
    val revision: Int?
)