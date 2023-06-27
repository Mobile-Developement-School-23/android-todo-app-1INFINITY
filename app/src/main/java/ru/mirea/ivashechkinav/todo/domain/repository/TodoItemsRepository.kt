package ru.mirea.ivashechkinav.todo.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    suspend fun addItem(item: TodoItem)

    suspend fun deleteItemById(id: String)

    suspend fun updateItem(item: TodoItem)

    suspend fun getTodoItemsFlowWith(isChecked: Boolean): Flow<List<TodoItem>>

    suspend fun getCountOfCompletedItems(): Int

    suspend fun getItemById(id: String): TodoItem?

    suspend fun pullItemsFromServer()

    fun getTodoItemsFlow(): Flow<List<TodoItem>>
}