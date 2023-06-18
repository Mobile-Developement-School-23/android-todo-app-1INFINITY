package ru.mirea.ivashechkinav.todo.presentation.models

import ru.mirea.ivashechkinav.todo.data.models.Importance

data class TodoItemUI(
    var id: String? = null,
    var text: String? = null,
    var importance: Importance = Importance.LOW,
    var deadlineTimestamp: Long? = null,
    var isComplete: Boolean = false,
    var creationTimestamp: Long? = null,
)