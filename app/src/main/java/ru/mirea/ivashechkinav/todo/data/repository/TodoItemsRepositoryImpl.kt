package ru.mirea.ivashechkinav.todo.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.LocalStorageException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.OutOfSyncDataException
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.core.UnauthorizedException
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.retrofit.TodoApi
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkRequest
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NetworkRequestList
import ru.mirea.ivashechkinav.todo.data.retrofit.models.toNetworkItem
import ru.mirea.ivashechkinav.todo.data.retrofit.models.toTodoItem
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import java.net.HttpURLConnection
import javax.inject.Inject

@AppScope
class TodoItemsRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val todoApi: TodoApi,
) : TodoItemsRepository {

    override suspend fun addItem(item: TodoItem): ResultData<Unit> =
        withIOContext {
            todoDao.save(item = item)
            todoApi.add(NetworkRequest(item.toNetworkItem()))
            ResultData.Success(Unit)
        }

    override suspend fun deleteItemById(id: String): ResultData<Unit> =
        withIOContext {
            todoDao.deleteById(itemId = id)
            todoApi.delete(id = id)
            ResultData.Success(Unit)
        }

    override suspend fun updateItem(item: TodoItem): ResultData<Unit> =
        withIOContext {
            todoDao.update(item = item)
            todoApi.update(id = item.id, itemRequest = NetworkRequest(item.toNetworkItem()))
            ResultData.Success(Unit)
        }

    override suspend fun getTodoItemsByCheckedState(isChecked: Boolean) =
        withContext(Dispatchers.IO) {
            if (isChecked) {
                return@withContext todoDao.getAllFlowUnchecked()
            }
            return@withContext todoDao.getAllFlow()
        }

    override suspend fun getCountOfCompletedItems(): Int =
        withContext(Dispatchers.IO) {
            return@withContext todoDao.getCompletedCount()
        }

    override suspend fun getItemById(id: String): ResultData<TodoItem> =
        withIOContext {
            todoDao.getById(itemId = id)?.let {
                return@withIOContext ResultData.Success(it)
            }
            ResultData.Failure(TodoItemNotFoundException())
        }

    override suspend fun toggleItemCheckedState(id: String, timestamp: Long): ResultData<Unit> =
        withIOContext {
            todoDao.toggleItemCompleteState(itemId = id, timestamp = timestamp)
            val item = todoDao.getById(id) ?: throw LocalStorageException()
            todoApi.update(id = id, NetworkRequest(item.toNetworkItem()))
            ResultData.Success(Unit)
        }

    override suspend fun syncItems(): ResultData<Unit> =
        withIOContext {
            val serverList = todoApi.getAll().list.map { it.toTodoItem() }
            serverList.forEach { todoDao.upsertItem(it) }
            todoApi.patch(NetworkRequestList(todoDao.getAll().map { it.toNetworkItem() }))
            ResultData.Success(Unit)
        }

    private suspend inline  fun <T> withIOContext(crossinline block: suspend () -> ResultData<T>): ResultData<T> {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext block()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    companion object {
        private fun handleException(e: Exception): ResultData.Failure {
            return if (e is HttpException) {
                ResultData.Failure(
                    e.response()?.errorBody()?.string().toString()
                        .let { message ->
                            when (e.code()) {
                                HttpURLConnection.HTTP_BAD_REQUEST -> when {
                                    message.contains("unsynchronized") -> OutOfSyncDataException()
                                    message.contains("duplicate") -> DuplicateItemException()
                                    else -> BadRequestException()
                                }

                                HttpURLConnection.HTTP_INTERNAL_ERROR -> ServerSideException()
                                HttpURLConnection.HTTP_NOT_FOUND -> TodoItemNotFoundException()
                                HttpURLConnection.HTTP_UNAUTHORIZED -> UnauthorizedException()
                                else -> e
                            }
                        }
                )
            } else if (e.message?.let { it.contains("hostname") || it.contains("timeout") } == true) {
                ResultData.Failure(NetworkException())
            } else {
                ResultData.Failure(e)
            }
        }
    }
}
