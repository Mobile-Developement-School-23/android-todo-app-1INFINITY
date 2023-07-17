package ru.mirea.ivashechkinav.todo.data.retrofit.models

@kotlinx.serialization.Serializable
data class NetworkRequest(
    val element: NWTodoItem,
)