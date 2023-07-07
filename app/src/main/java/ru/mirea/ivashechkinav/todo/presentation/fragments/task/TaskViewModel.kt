package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewModel
import java.util.*
import javax.inject.Inject

class TaskViewModel @Inject constructor(repository: TodoItemsRepository) : ViewModel() {
    sealed class EventUi {
        object OnCancelButtonClicked : EventUi()
        object OnSaveButtonClicked : EventUi()
        object OnDeleteButtonClicked : EventUi()
        object OnBackButtonClicked : EventUi()
        data class OnTodoTextEdited(val editedText: String) : EventUi()
        data class OnImportanceSelected(val importance: Importance) : EventUi()
        data class OnDeadlineSelected(val timestamp: Long) : EventUi()
        data class OnDeadlineSwitchChanged(val isChecked: Boolean) : EventUi()
        data class OnTodoItemIdLoaded(val todoItemId: String) : EventUi()
    }

    sealed class FragmentViewState {
        object Loading : FragmentViewState()
        object Update : FragmentViewState()
    }

    sealed class EffectUi {
        data class ShowSnackbar(val message: String) : EffectUi()
        object ToBackFragment : EffectUi()
        object ShowDatePicker : EffectUi()
    }

    data class UiState(
        var id: String? = null,
        var text: String? = null,
        var importance: Importance = Importance.LOW,
        var deadlineTimestamp: Long? = null,
        var isComplete: Boolean = false,
        var creationTimestamp: Long? = null,
        val viewState: FragmentViewState = FragmentViewState.Loading
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

    init {
        viewModelScope.launch(exceptionHandler) {
            _event.collect { event ->
                when (event) {
                    is EventUi.OnBackButtonClicked -> setEffect { EffectUi.ToBackFragment }
                    is EventUi.OnCancelButtonClicked -> setEffect { EffectUi.ToBackFragment }
                    is EventUi.OnSaveButtonClicked -> {

                        val newItem = validateSaveTask() ?: return@collect

                        if (uiState.value.id == null) {
                            repository.addItem(newItem)
                        } else {
                            repository.updateItem(newItem)
                        }
                        setEffect { EffectUi.ToBackFragment }
                    }
                    is EventUi.OnDeleteButtonClicked -> {
                        val currentTodoItem = uiState.value
                        currentTodoItem.id?.let {
                            repository.deleteItemById(it)
                        }
                        setEffect { EffectUi.ToBackFragment }
                    }
                    is EventUi.OnTodoTextEdited -> {
                        setState {
                            copy(
                                text = event.editedText,
                                viewState = FragmentViewState.Update
                            )
                        }
                    }
                    is EventUi.OnImportanceSelected -> {
                        setState {
                            copy(importance = event.importance)
                        }
                    }
                    is EventUi.OnDeadlineSelected -> {
                        setState {
                            copy(deadlineTimestamp = event.timestamp)
                        }
                    }
                    is EventUi.OnDeadlineSwitchChanged -> {
                        if (event.isChecked) {
                            setEffect { EffectUi.ShowDatePicker }
                            return@collect
                        }
                        setState {
                            copy(deadlineTimestamp = null)
                        }
                    }
                    is EventUi.OnTodoItemIdLoaded -> {
                        val result = repository.getItemById(event.todoItemId)
                        if (result is ResultData.Success){
                            val todoItem = result.value ?: return@collect
                            setState {
                                copy(
                                    id = todoItem.id,
                                    text = todoItem.text,
                                    importance = todoItem.importance,
                                    deadlineTimestamp = todoItem.deadlineTimestamp,
                                    isComplete = todoItem.isComplete,
                                    creationTimestamp = todoItem.creationTimestamp
                                )
                            }
                        }
                    }
                }
            }
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

    private fun validateSaveTask(): TodoItem? {
        val currentTime = System.currentTimeMillis() / 1000
        val currentTodoItem = uiState.value
        if (currentTodoItem.text.isNullOrEmpty()) {
            setEffect { EffectUi.ShowSnackbar("Необходимо ввести описание дела") }
            return null
        }
        return TodoItem(
            id = currentTodoItem.id ?: UUID.randomUUID().toString(),
            text = currentTodoItem.text ?: "",
            importance = currentTodoItem.importance,
            deadlineTimestamp = currentTodoItem.deadlineTimestamp,
            isComplete = currentTodoItem.isComplete,
            creationTimestamp = currentTodoItem.creationTimestamp ?: currentTime,
            changeTimestamp = currentTime
        )
    }
}