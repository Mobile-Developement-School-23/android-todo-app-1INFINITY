package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.OperationRepeatHandler
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TextHelper
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiEffect
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiEvent
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.FragmentViewState
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiState
import java.util.UUID
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val repository: TodoItemsRepository,
    private val textHelper: TextHelper,
    private val handler: OperationRepeatHandler
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }
    private val scope = viewModelScope + exceptionHandler
    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect: Channel<UiEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private fun setState(reduce: UiState.() -> UiState) {
        val newState = uiState.value.reduce()
        _uiState.value = newState
    }

    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    private fun setEffect(builder: () -> UiEffect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    init {
        scope.launch {
            _event.collect { handleEvent(it) }
        }
    }

    private suspend fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.OnBackButtonClicked -> setEffect { UiEffect.ToBackFragment }
            is UiEvent.OnCancelButtonClicked -> setEffect { UiEffect.ToBackFragment }
            is UiEvent.OnSaveButtonClicked -> onSaveButtonClicked()
            is UiEvent.OnDeleteButtonClicked -> onDeleteButtonClicked()
            is UiEvent.OnTodoTextEdited -> onTodoTextEdited(event)
            is UiEvent.OnImportanceSelected -> onImportanceSelected(event)
            is UiEvent.OnDeadlineSelected -> onDeadlineSelected(event)
            is UiEvent.OnDeadlineSwitchChanged -> onDeadlineSwitchChanged(event)
            is UiEvent.OnTodoItemIdLoaded -> onTodoItemIdLoaded(event)
        }
    }

    private suspend fun onSaveButtonClicked() {
        val newItem = validateSaveTask() ?: return

        if (uiState.value.id == null) {
            handler.retryWithAttempts { repository.addItem(newItem) }
        } else {
            handler.retryWithAttempts { repository.updateItem(newItem) }
        }
        setEffect { UiEffect.ToBackFragment }
    }

    private suspend fun onDeleteButtonClicked() {
        val currentTodoItem = uiState.value
        currentTodoItem.id?.let {
            handler.retryWithAttempts { repository.deleteItemById(it) }
        }
        setEffect { UiEffect.ToBackFragment }
    }

    private fun onTodoTextEdited(event: UiEvent.OnTodoTextEdited) {
        setState {
            copy(
                text = event.editedText, viewState = FragmentViewState.Update
            )
        }
    }

    private fun onImportanceSelected(event: UiEvent.OnImportanceSelected) {
        setState {
            copy(importance = event.importance)
        }
    }

    private fun onDeadlineSelected(event: UiEvent.OnDeadlineSelected) {
        setState {
            copy(deadlineTimestamp = event.timestamp)
        }
    }

    private fun onDeadlineSwitchChanged(event: UiEvent.OnDeadlineSwitchChanged) {
        if (event.isChecked) {
            setEffect { UiEffect.ShowDatePicker }
            return
        }
        setState {
            copy(deadlineTimestamp = null)
        }
    }

    private suspend fun onTodoItemIdLoaded(event: UiEvent.OnTodoItemIdLoaded) {
        if(uiState.value.id == event.todoItemId)
            return

        val result = handler.retryWithAttempts { repository.getItemById(event.todoItemId) }
        if (result is ResultData.Success) {
            val todoItem = result.value ?: return
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

    private fun handleException(e: Throwable) {
        val errorText = when (e) {
            is HttpException, is NetworkException -> textHelper.getString(R.string.connection_missing_message)
            is ServerSideException,
            is BadRequestException,
            is TodoItemNotFoundException,
            is DuplicateItemException -> textHelper.getString(
                R.string.server_error_message
            )

            else -> textHelper.getString(R.string.unknown_error_message)
        }
        setEffect { UiEffect.ShowSnackbar(message = errorText) }
    }

    private fun validateSaveTask(): TodoItem? {
        val currentTime = System.currentTimeMillis() / SECONDS_DIVIDER
        val currentTodoItem = uiState.value
        if (currentTodoItem.text.isNullOrEmpty()) {
            setEffect { UiEffect.ShowSnackbar(textHelper.getString(R.string.description_needed_message)) }
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

    companion object {
        const val SECONDS_DIVIDER = 1000
    }
}
