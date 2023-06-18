package ru.mirea.ivashechkinav.todo.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    suspend fun addItem(item: TodoItem): Boolean

    suspend fun deleteItemById(id: String): Boolean

    suspend fun updateItem(item: TodoItem): Boolean

    fun getAllItems(): List<TodoItem>

    fun getItemById(id: String): TodoItem?

    fun getTodoItemsFlow(): Flow<List<TodoItem>>
}