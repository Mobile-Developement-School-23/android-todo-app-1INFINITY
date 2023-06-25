package ru.mirea.ivashechkinav.todo.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todoItems")
data class TodoItem(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val text: String,
    val importance: Importance,
    val deadlineTimestamp: Long?,
    val isComplete: Boolean,
    val creationTimestamp: Long,
    val changeTimestamp: Long,
)
