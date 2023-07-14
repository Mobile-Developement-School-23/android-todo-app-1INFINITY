package ru.mirea.ivashechkinav.todo.data.retrofit.models

@kotlinx.serialization.Serializable
data class NWRequestList(
    val list: List<NWTodoItem>,
)