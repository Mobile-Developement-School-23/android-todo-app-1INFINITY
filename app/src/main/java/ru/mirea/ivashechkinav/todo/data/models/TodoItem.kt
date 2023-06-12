package ru.mirea.ivashechkinav.todo.data.models

data class TodoItem(
    val id: String,
    val text: String,
    val importance: Importance,
    val deadlineTimestamp: Long?,
    val isComplete: Boolean,
    val creationTimestamp: Long,
    val changeTimestamp: Long,
)
