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
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWRequest
import ru.mirea.ivashechkinav.todo.data.retrofit.models.NWRequestList
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
        withContext(Dispatchers.IO) {
            return@withContext try {
                todoDao.save(item = item)
                todoApi.add(NWRequest(item.toNetworkItem()))
                ResultData.Success(Unit)
            } catch (e: Exception) {
                handleException(e)
            }
        }

    override suspend fun deleteItemById(id: String): ResultData<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                todoDao.deleteById(itemId = id)
                todoApi.delete(id = id)
                ResultData.Success(Unit)
            } catch (e: Exception) {
                handleException(e)
            }
        }

    override suspend fun updateItem(item: TodoItem): ResultData<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                todoDao.update(item = item)
                todoApi.update(id = item.id, itemRequest = NWRequest(item.toNetworkItem()))
                ResultData.Success(Unit)
            } catch (e: Exception) {
                handleException(e)
            }
        }

    override suspend fun getTodoItemsFlowWith(isChecked: Boolean) =
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
        withContext(Dispatchers.IO) {
            try {
                todoDao.getById(itemId = id)?.let {
                    return@withContext ResultData.Success(it)
                }
                return@withContext ResultData.Failure(TodoItemNotFoundException())
            } catch (e: Exception) {
                return@withContext handleException(e)
            }
        }

    override suspend fun toggleItemCheckedState(id: String, timestamp: Long): ResultData<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                todoDao.toggleItemCompleteState(itemId = id, timestamp = timestamp)
                val item = todoDao.getById(id) ?: throw LocalStorageException()
                todoApi.update(id = id, NWRequest(item.toNetworkItem()))
                ResultData.Success(Unit)
            } catch (e: Exception) {
                handleException(e)
            }
        }

    override suspend fun syncItems(): ResultData<Unit> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val serverList = todoApi.getAll().list.map { it.toTodoItem() }
                serverList.forEach { todoDao.upsertItem(it) }
                todoApi.patch(
                    NWRequestList(todoDao.getAll().map { it.toNetworkItem() })
                )
                ResultData.Success(Unit)
            } catch (e: Exception) {
                handleException(e)
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
