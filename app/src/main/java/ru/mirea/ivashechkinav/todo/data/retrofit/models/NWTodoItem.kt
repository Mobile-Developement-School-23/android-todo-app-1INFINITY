package ru.mirea.ivashechkinav.todo.data.retrofit.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.models.parseImportanceFromNetwork
import ru.mirea.ivashechkinav.todo.data.models.toNetworkFormat


@Serializable
data class NWTodoItem(
    val id: String,
    val text: String,
    val importance: String,
    val deadline: Long? = null,
    val done: Boolean,
    val color: String? = null,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("changed_at")
    val changedAt: Long,
    @SerialName("last_updated_by")
    val lastUpdatedBy: String
)

fun TodoItem.toNetworkItem(): NWTodoItem {
    return NWTodoItem(
        id = id,
        text = text,
        importance = importance.toNetworkFormat(),
        deadline = deadlineTimestamp,
        done = isComplete,
        createdAt = creationTimestamp,
        changedAt = changeTimestamp,
        lastUpdatedBy = "cd567"
    )
}

fun NWTodoItem.toTodoItem(): TodoItem {
    return TodoItem(
        id = id,
        text = text,
        importance = parseImportanceFromNetwork(importance),
        deadlineTimestamp = deadline,
        isComplete = done,
        creationTimestamp = createdAt,
        changeTimestamp = changedAt
    )
}