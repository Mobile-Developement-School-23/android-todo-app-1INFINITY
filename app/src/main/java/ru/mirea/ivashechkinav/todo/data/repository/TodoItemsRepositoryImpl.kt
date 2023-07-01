package ru.mirea.ivashechkinav.todo.data.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.retrofit.*
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import ru.mirea.ivashechkinav.todo.data.sharedprefs.RevisionRepository
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class TodoItemsRepositoryImpl(
    private val todoDao: TodoDao,
    private val todoApi: TodoApi,
    private val revisionRepository: RevisionRepository
) : TodoItemsRepository {

    companion object {
        const val MAX_ATTEMPTS = 3
    }

    suspend fun <T> retryWithAttempts(
        attempts: Int,
        errorMessage: String,
        block: suspend () -> ResultData<T>
    ): ResultData<T> {
        var remainingAttempts = attempts
        var error: Exception? = null
        while (remainingAttempts > 0) {
            try {
                return block()
            } catch (e: Exception) {
                println("Ошибка: ${e.message}. Повторная попытка...")
                error = e
                remainingAttempts--
            }
        }
        return if (error != null)
            ResultData.failure(getErrorMessage(error))
        else
            ResultData.failure(errorMessage)
    }
    private fun getErrorMessage(e: Exception): String {
        if (e is HttpException) {
            return when (e.code()) {
                400 -> {
                   "Ошибка на сервере с ревизией. Сообщите в поддержку"
                }
                401 -> {
                    "Ошибка на сервере с авторизацией. Сообщите в поддержку"
                }
                404 -> {
                    "Элемента нет на сервере. Сообщите в поддержку"
                }
                500 -> {
                   "Неизвестная ошибка сервера"
                }
                else -> {
                 "Неизвестная Http ошибка"
                }
            }
        } else {
            return "Нету соединения с интернетом"
        }
    }
    override suspend fun addItem(item: TodoItem): ResultData<Nothing> = withContext(Dispatchers.IO) {
        todoDao.save(item = item)


        val request = NWRequest(
            element = item.toNetworkItem()
        )
        return@withContext retryWithAttempts(attempts = MAX_ATTEMPTS, errorMessage = "Ошибка при добавлении дела") {
            val lastRevision = revisionRepository.getLastRevision()
            val response = todoApi.add(revision = lastRevision, itemRequest = request)
            revisionRepository.setRevision(response.revision!!)
            return@retryWithAttempts ResultData.success<Nothing>()
        }.also { if(it is ResultData.Failure) revisionRepository.editLocalChanges(true) }
    }

    override suspend fun deleteItemById(id: String): ResultData<Nothing> = withContext(Dispatchers.IO) {
        todoDao.deleteById(itemId = id)
        return@withContext retryWithAttempts(attempts = MAX_ATTEMPTS, errorMessage = "Ошибка при удалении дела") {
            val lastRevision = revisionRepository.getLastRevision()
            val response = todoApi.delete(revision = lastRevision, id = id)
            revisionRepository.setRevision(response.revision!!)
            return@retryWithAttempts ResultData.success<Nothing>()
        }.also { if(it is ResultData.Failure) revisionRepository.editLocalChanges(true) }
    }

    override fun getTodoItemsFlow(): Flow<List<TodoItem>> {
        return todoDao.getAllFlow()
    }

    override suspend fun updateItem(item: TodoItem): ResultData<Nothing> = withContext(Dispatchers.IO)  {
        val itemToUpdate = todoDao.getById(itemId = item.id)

        itemToUpdate?.let {
            val updatedItem = it.copy(
                text = item.text,
                importance = item.importance,
                deadlineTimestamp = item.deadlineTimestamp,
                isComplete = item.isComplete,
                changeTimestamp = System.currentTimeMillis()
            )
            todoDao.update(item = updatedItem)
            return@withContext retryWithAttempts(MAX_ATTEMPTS, "Ошибка при изменении дела") {
                val lastRevision = revisionRepository.getLastRevision()
                val nwRequest = NWRequest(
                    element = item.toNetworkItem()
                )
                val response =
                    todoApi.update(revision = lastRevision, id = item.id, itemRequest = nwRequest)
                revisionRepository.setRevision(response.revision!!)
                return@retryWithAttempts ResultData.success<Nothing>()
            }.also { if(it is ResultData.Failure) revisionRepository.editLocalChanges(true) }
        }
        return@withContext ResultData.failure("Ошибка при изменении дела")
    }

    override suspend fun getTodoItemsFlowWith(isChecked: Boolean) = withContext(Dispatchers.IO) {
        if (isChecked) {
            return@withContext todoDao.getAllFlowWithCheckedState(isChecked = false)
        }
        return@withContext todoDao.getAllFlow()
    }

    override suspend fun getCountOfCompletedItems(): Int = withContext(Dispatchers.IO) {
        return@withContext todoDao.getCompletedCount()
    }

    override suspend fun getItemById(id: String): ResultData<TodoItem> = withContext(Dispatchers.IO) {

        retryWithAttempts(MAX_ATTEMPTS, "Ошибка при получении дела") {
            val response = todoApi.getByID(id = id)
            response.revision?.let {
                revisionRepository.setRevision(it)
            }
            return@retryWithAttempts ResultData.success(todoDao.getById(itemId = id))
        }

    }

    override suspend fun pullItemsFromServer(): ResultData<Nothing> = withContext(Dispatchers.IO) {
        retryWithAttempts(MAX_ATTEMPTS, "Ошибка при обновлении данных") {
            val response = todoApi.getAll()
            response.revision?.let {
                revisionRepository.setRevision(it)
            }
            todoDao.deleteAll()
            val newList = response.list?.map {
                it.toTodoItem() ?: throw IllegalArgumentException("Item from server can not be null")
            } ?: throw IllegalArgumentException("List from server can not be null ")

            todoDao.save(newList)
            revisionRepository.editLocalChanges(false)
            return@retryWithAttempts ResultData.success<Nothing>()
        }.also { if(it is ResultData.Success) revisionRepository.editLocalChanges(false) }
    }
    override suspend fun patchItemsToServer(): ResultData<Nothing> = withContext(Dispatchers.IO) {
        retryWithAttempts(MAX_ATTEMPTS, "Ошибка при загрузке локальных изменения на сервер") {
            //PATCH перезаписывает все, что было в репе(условие такое)
            if (!revisionRepository.hasLocalChanges())
                return@retryWithAttempts ResultData.failure("Нету локальных изменений")
            val revision = todoApi.getAll().revision!!
            val roomItems = todoDao.getAll().map { it.toNetworkItem() }
            val nwRequestList = NWRequestList(
                list = roomItems
            )
            val response = todoApi.patch(revision, nwRequestList)
            revisionRepository.setRevision(response.revision!!)
            return@retryWithAttempts ResultData.success()
        }
    }

    private fun generateItems(): MutableList<TodoItem> {
        return mutableListOf(
            TodoItem(
                id = "1",
                text = "Buy groceries:\nMilk\nBread\nEggs\nBeacon",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "2",
                text = "Buy groceries:\nMilk\nBread\nEggs\nBeacon",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = true,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "3",
                text = "Read a book",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000, // Set deadline for a month later
                isComplete = true,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "4",
                text = "Call mom",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "5",
                text = "Fix leaking faucet",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000, // Set deadline for two days later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "6",
                text = "Attend a meeting",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "7",
                text = "Pay bills",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000, // Set deadline for a month later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "8",
                text = "Write a blog post",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "9",
                text = "Plan vacation",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "10",
                text = "Clean the garage",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000, // Set deadline for two days later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "11",
                text = "Finish the report for the quarterly meeting",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "12",
                text = "Prepare a presentation for the project pitch",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "13",
                text = "Research and gather data for market analysis",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "14",
                text = "Organize team building activities for the department",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ), TodoItem(
                id = "15",
                text = "Implement new feature based on user feedback and requirements",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 604800000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            )
        )
    }
}