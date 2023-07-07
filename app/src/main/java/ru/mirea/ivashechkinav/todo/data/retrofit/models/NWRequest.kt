package ru.mirea.ivashechkinav.todo.data.retrofit.models

@kotlinx.serialization.Serializable
data class NWRequest(
    val element: NWTodoItem,
)