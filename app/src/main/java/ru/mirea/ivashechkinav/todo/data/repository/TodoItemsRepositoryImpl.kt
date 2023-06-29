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

    init {
        GlobalScope.launch(Dispatchers.IO) {
            todoDao.deleteAll()
            //todoDao.save(generateItems())
            val response = todoApi.getAll()
            todoDao.save(response.list!!.map { it.toTodoItem()!! })
            revisionRepository.setRevision(response.revision!!)
        }
    }

    private fun <T> Flow<ResultData<T>>.onHttpError(): Flow<ResultData<T>> {
        return catch { cause ->
            if (cause is HttpException) {
                when (cause.code()) {
                    400 -> {
                        emit(ResultData.failure("400"))
                    }
                    401 -> {
                        emit(ResultData.failure("401"))
                    }
                    404 -> {
                        emit(ResultData.failure("404"))
                    }
                    500 -> {
                        emit(ResultData.failure("500"))
                    }
                    else -> {
                        emit(ResultData.failure("Неизвестная Http ошибка"))
                    }
                }
            } else {
                emit(ResultData.failure("Неизвестная ошибка"))
            }
        }
    }

    override suspend fun addItem(item: TodoItem) = flow<ResultData<Nothing>> {
        emit(ResultData.loading())
        //Local
        todoDao.save(item = item)

        //Network
        val lastRevision = revisionRepository.getLastRevision()
        val request = NWRequest(
            element = item.toNetworkItem()
        )
        val response = todoApi.add(revision = lastRevision, itemRequest = request)
        revisionRepository.setRevision(response.revision!!)
        if (response.element != null)
            emit(ResultData.success())
        else
            emit(ResultData.failure("Ошибка при добавлении нового дела"))
    }
        .flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            delay(1000)
            attempt < MAX_ATTEMPTS && cause is HttpException
        }
        .onHttpError()

    override suspend fun deleteItemById(id: String) = flow<ResultData<Nothing>> {
        emit(ResultData.loading())
        //Local
        todoDao.deleteById(itemId = id)

        //Network
        val lastRevision = revisionRepository.getLastRevision()
        val response = todoApi.delete(revision = lastRevision, id = id)
        revisionRepository.setRevision(response.revision!!)
        emit(ResultData.success())
    }
        .flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            delay(1000)
            attempt < MAX_ATTEMPTS && cause is HttpException
        }
        .onHttpError()

    override fun getTodoItemsFlow(): Flow<List<TodoItem>> {
        return todoDao.getAllFlow()
    }

    override suspend fun updateItem(item: TodoItem) = flow<ResultData<Nothing>> {
        emit(ResultData.loading())
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

            val lastRevision = revisionRepository.getLastRevision()
            val nwRequest = NWRequest(
                element = item.toNetworkItem()
            )
            val response =
                todoApi.update(revision = lastRevision, id = item.id, itemRequest = nwRequest)
            revisionRepository.setRevision(response.revision!!)
            emit(ResultData.success())
        }
    }
        .flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            delay(1000)
            attempt < MAX_ATTEMPTS && cause is HttpException
        }
        .onHttpError()

    override suspend fun getTodoItemsFlowWith(isChecked: Boolean) = withContext(Dispatchers.IO) {
        if (isChecked) {
            return@withContext todoDao.getAllFlowWithCheckedState(isChecked = false)
        }
        return@withContext todoDao.getAllFlow()
    }

    override suspend fun getCountOfCompletedItems(): Int = withContext(Dispatchers.IO) {
        return@withContext todoDao.getCompletedCount()
    }

    override suspend fun getItemById(id: String) = flow<ResultData<TodoItem>> {
        emit(ResultData.loading())
        val response = todoApi.getByID(id = id)
        response.revision?.let {
            revisionRepository.setRevision(it)
        }
        val item = todoDao.getById(itemId = id)
        emit(ResultData.success(item))
    }
        .flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            delay(1000)
            attempt < MAX_ATTEMPTS && cause is HttpException
        }
        .onHttpError()

    override suspend fun pullItemsFromServer() = flow<ResultData<Nothing>> {
        emit(ResultData.loading())
        val response = todoApi.getAll()
        response.revision?.let {
            revisionRepository.setRevision(it)
        }
        todoDao.deleteAll()
        val newList = response.list?.map {
            it.toTodoItem() ?: throw IllegalArgumentException("Item from server can not be null")
        } ?: throw IllegalArgumentException("List from server can not be null ")

        todoDao.save(newList)
        emit(ResultData.success())
    }
        .flowOn(Dispatchers.IO)
        .retryWhen { cause, attempt ->
            delay(1000)
            attempt < MAX_ATTEMPTS && cause is HttpException
        }
        .onHttpError()

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