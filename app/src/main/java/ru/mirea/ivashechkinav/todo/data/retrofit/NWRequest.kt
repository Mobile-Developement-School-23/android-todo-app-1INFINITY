package ru.mirea.ivashechkinav.todo.data.retrofit

@kotlinx.serialization.Serializable
data class NWRequest(
    val element: NWTodoItem,
)