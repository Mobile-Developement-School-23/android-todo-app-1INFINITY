package ru.mirea.ivashechkinav.todo.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    suspend fun addItem(item: TodoItem): Flow<ResultData<Nothing>>

    suspend fun deleteItemById(id: String): Flow<ResultData<Nothing>>

    suspend fun updateItem(item: TodoItem): Flow<ResultData<Nothing>>

    suspend fun getTodoItemsFlowWith(isChecked: Boolean): Flow<List<TodoItem>>

    suspend fun getCountOfCompletedItems(): Int

    suspend fun getItemById(id: String): Flow<ResultData<TodoItem>>

    suspend fun pullItemsFromServer(): Flow<ResultData<Nothing>>

    fun getTodoItemsFlow(): Flow<List<TodoItem>>
}