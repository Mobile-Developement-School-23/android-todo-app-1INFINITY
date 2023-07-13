package ru.mirea.ivashechkinav.todo.data.retrofit.models

@kotlinx.serialization.Serializable
data class NWRequest( // NW = network, not obvious
    val element: NWTodoItem,
)