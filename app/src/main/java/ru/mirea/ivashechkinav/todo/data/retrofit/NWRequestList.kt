package ru.mirea.ivashechkinav.todo.data.retrofit

@kotlinx.serialization.Serializable
data class NWRequestList(
    val list: List<NWTodoItem>,
)