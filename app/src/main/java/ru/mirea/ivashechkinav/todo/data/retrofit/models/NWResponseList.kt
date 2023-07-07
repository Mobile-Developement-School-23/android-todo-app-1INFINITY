package ru.mirea.ivashechkinav.todo.data.retrofit.models

import kotlinx.serialization.Serializable


@Serializable
data class NWResponseList(
    val status: String?,
    val list: List<NWTodoItem>?,
    val revision: Int?
)
