package ru.mirea.ivashechkinav.todo.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    suspend fun addItem(item: TodoItem): ResultData<Unit>

    suspend fun deleteItemById(id: String): ResultData<Unit>

    suspend fun updateItem(item: TodoItem): ResultData<Unit>

    suspend fun getTodoItemsFlowWith(isChecked: Boolean): Flow<List<TodoItem>>

    suspend fun getCountOfCompletedItems(): Int

    suspend fun getItemById(id: String): ResultData<TodoItem>

    suspend fun pullItemsFromServer(): ResultData<Unit>

    fun getTodoItemsFlow(): Flow<List<TodoItem>>

    suspend fun patchItemsToServer(): ResultData<Unit>

    suspend fun syncItems(): ResultData<Unit>
}