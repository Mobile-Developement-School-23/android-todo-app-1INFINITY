package ru.mirea.ivashechkinav.todo.data.retrofit.models

@kotlinx.serialization.Serializable
data class NetworkRequestList(
    val list: List<NWTodoItem>,
)