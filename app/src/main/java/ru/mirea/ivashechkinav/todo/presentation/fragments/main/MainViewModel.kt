package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.core.retryWithAttempts
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.receiver.NetworkChangeReceiver
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val repository: TodoItemsRepository,
    private val networkChangeReceiver: NetworkChangeReceiver
) : ViewModel() {
    sealed class EventUi {
        data class OnVisibleChange(val isFilterCompleted: Boolean) : EventUi()
        data class OnItemSelected(val todoItem: TodoItem) : EventUi()
        data class OnItemCheckedChange(val todoItem: TodoItem) : EventUi()
        data class OnItemSwipeToDelete(val todoItem: TodoItem) : EventUi()
        data class OnItemSwipeToCheck(val todoItem: TodoItem) : EventUi()
        object OnFloatingButtonClick : EventUi()
        object OnSnackBarPullRetryButtonClicked : EventUi()
    }

    sealed class EffectUi {
        data class ShowSnackbar(val message: String) : EffectUi()
        data class ToTaskFragmentUpdate(val todoItemId: String) : EffectUi()
        object ToTaskFragmentCreate : EffectUi()
        object ShowSnackbarWithPullRetry : EffectUi()
    }

    data class UiState(
        val countOfCompletedText: String = "Загрузка выполненных задач...",
        val todoItems: List<TodoItem> = listOf(),
        val isFilterCompleted: Boolean = false
    )

    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }

    private val _event: MutableSharedFlow<EventUi> = MutableSharedFlow()

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect: Channel<EffectUi> = Channel()
    val effect = _effect.receiveAsFlow()

    private fun setState(reduce: UiState.() -> UiState) {
        val newState = uiState.value.reduce()
        _uiState.value = newState
    }

    fun setEvent(event: EventUi) = viewModelScope.launch { _event.emit(event) }

    private fun setEffect(builder: () -> EffectUi) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    private val visibleStateFlow = MutableStateFlow(false)
    private val itemsFlow = visibleStateFlow.flatMapLatest {
        repository.getTodoItemsFlowWith(isChecked = it)
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            pullItemsFromServer()

            networkChangeReceiver.stateFlow.collectLatest { isConnected ->
                if (!isConnected) {
                    setEffect { EffectUi.ShowSnackbar("Нет соединения с интернетом") }
                } else {
                    setEffect { EffectUi.ShowSnackbar("Cоединение с интернетом появилось") }
                    val result = retryWithAttempts{ repository.patchItemsToServer() }
                    if (result is ResultData.Failure)
                        pullItemsFromServer()
                }
            }
        }
        viewModelScope.launch(exceptionHandler) {
            itemsFlow.collect { list ->
                val count = repository.getCountOfCompletedItems()
                if (uiState.value.isFilterCompleted) {
                    setState {
                        copy(
                            countOfCompletedText = "Скрыто выполненных - $count",
                            todoItems = list.filter { !it.isComplete }
                        )
                    }
                } else {
                    setState {
                        copy(
                            countOfCompletedText = "Выполнено - $count",
                            todoItems = list
                        )
                    }
                }
            }
        }
        viewModelScope.launch(exceptionHandler) {
            _event.collect { event ->
                when (event) {
                    is EventUi.OnVisibleChange -> {
                        visibleStateFlow.value = event.isFilterCompleted
                        setState {
                            copy(
                                isFilterCompleted = event.isFilterCompleted
                            )
                        }
                    }

                    is EventUi.OnItemSelected -> {
                        val itemId = event.todoItem.id
                        setEffect { EffectUi.ToTaskFragmentUpdate(itemId) }
                    }

                    is EventUi.OnItemSwipeToCheck -> {
                        val itemChecked = event.todoItem
                            .copy(isComplete = !event.todoItem.isComplete)
                        retryWithAttempts { repository.updateItem(itemChecked) }
                    }

                    is EventUi.OnItemCheckedChange -> {
                        val itemChecked = event.todoItem
                            .copy(isComplete = !event.todoItem.isComplete)
                        retryWithAttempts { repository.updateItem(itemChecked) }
                    }

                    is EventUi.OnItemSwipeToDelete -> {
                        retryWithAttempts { repository.deleteItemById(event.todoItem.id) }
                    }

                    is EventUi.OnFloatingButtonClick -> {
                        setEffect { EffectUi.ToTaskFragmentCreate }
                    }

                    is EventUi.OnSnackBarPullRetryButtonClicked -> pullItemsFromServer()
                }
            }
        }
    }

    private suspend fun pullItemsFromServer() {
        val result = retryWithAttempts{ repository.pullItemsFromServer() }
        if (result is ResultData.Failure) {
            setEffect { EffectUi.ShowSnackbarWithPullRetry }
        }
    }

    private fun handleException(e: Throwable) {
        val errorText =
            when (e) {
                is HttpException, is NetworkException -> "Отсутствует подключение"

                is ServerSideException,
                is BadRequestException,
                is TodoItemNotFoundException,
                is DuplicateItemException
                -> "Ошибка на сервере"

                else -> "Неизвестная ошибка"
            }
        setEffect { EffectUi.ShowSnackbar(message = errorText) }
    }
}