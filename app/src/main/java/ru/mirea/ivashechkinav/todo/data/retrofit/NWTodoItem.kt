package ru.mirea.ivashechkinav.todo.data.retrofit

import kotlinx.serialization.Serializable
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.models.parseImportanceFromNetwork
import ru.mirea.ivashechkinav.todo.data.models.toNetworkFormat


@Serializable
data class NWTodoItem(
    val id: String? = null,
    val text: String? = null,
    val importance: String? = null,
    val deadline: Long? = null,
    val done: Boolean? = null,
    val color: String? = null,
    val created_at: Long? = null,
    val changed_at: Long? = null,
    val last_updated_by: String? = null
)

fun TodoItem.toNetworkItem(): NWTodoItem {
    return NWTodoItem(
        id = id,
        text = text,
        importance = importance.toNetworkFormat(),
        deadline = deadlineTimestamp,
        done = isComplete,
        created_at = creationTimestamp,
        changed_at = changeTimestamp,
        last_updated_by = "cd567"
    )
}

fun NWTodoItem.toTodoItem(): TodoItem? {
    return TodoItem(
        id = id ?: return null,
        text = text ?: return null,
        importance = parseImportanceFromNetwork(importance ?: return null),
        deadlineTimestamp = deadline,
        isComplete = done ?: return null,
        creationTimestamp = created_at ?: return null,
        changeTimestamp = changed_at ?: return null
    )
}