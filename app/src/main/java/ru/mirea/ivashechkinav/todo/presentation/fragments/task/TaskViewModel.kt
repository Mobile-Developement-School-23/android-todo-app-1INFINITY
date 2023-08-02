package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.OperationRepeatHandler
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.FragmentViewState
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiEffect
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiState
import java.util.UUID
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val repository: TodoItemsRepository,
    private val handler: OperationRepeatHandler
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }
    private val scope = viewModelScope + exceptionHandler

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect: Channel<UiEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private fun setState(reduce: UiState.() -> UiState) {
        val newState = uiState.value.reduce()
        _uiState.value = newState
    }

    private fun setEffect(builder: () -> UiEffect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    fun saveButtonClicked() = scope.launch {
        val newItem = validateSaveTask() ?: return@launch

        if (uiState.value.id == null) {
            handler.retryWithAttempts { repository.addItem(newItem) }
        } else {
            handler.retryWithAttempts { repository.updateItem(newItem) }
        }
        setEffect { UiEffect.ToBackFragment }
    }

    fun deleteButtonClicked() = scope.launch {
        val currentTodoItem = uiState.value
        currentTodoItem.id?.let {
            handler.retryWithAttempts { repository.deleteItemById(it) }
        }
        setEffect { UiEffect.ToBackFragment }
    }

    fun todoTextEdited(editedText: String) = scope.launch {
        setState {
            copy(
                text = editedText, viewState = FragmentViewState.Update
            )
        }
    }

    fun importanceSelected(importance: Importance) = scope.launch {
        setState {
            copy(importance = importance)
        }
    }

    fun onDeadlineSelected(timestamp: Long) = scope.launch {
        setState {
            copy(deadlineTimestamp = timestamp)
        }
    }

    fun deadlineSwitchChanged(isChecked: Boolean) = scope.launch {
        if (isChecked) {
            setEffect { UiEffect.ShowDatePicker }
            return@launch
        }
        setState {
            copy(deadlineTimestamp = null)
        }
    }
    fun cancelButtonClicked() = scope.launch {
        setEffect { UiEffect.ToBackFragment }
    }

    fun onTodoItemIdLoaded(itemId: String)  = scope.launch {
        if (uiState.value.id == itemId)
            return@launch

        val result = handler.retryWithAttempts { repository.getItemById(itemId) }
        if (result is ResultData.Success) {
            val todoItem = result.value ?: return@launch
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
        val message = when (e) {
            is HttpException, is NetworkException -> TaskContract.SnackbarMessage.ConnectionMissing
            is ServerSideException,
            is BadRequestException,
            is TodoItemNotFoundException,
            is DuplicateItemException -> TaskContract.SnackbarMessage.ServerError

            else -> TaskContract.SnackbarMessage.UnknownError
        }
        setEffect { UiEffect.ShowSnackbar(message) }
    }

    private fun validateSaveTask(): TodoItem? {
        val currentTime = System.currentTimeMillis() / SECONDS_DIVIDER
        val currentTodoItem = uiState.value
        if (currentTodoItem.text.isNullOrEmpty()) {
            setEffect { UiEffect.ShowSnackbar(TaskContract.SnackbarMessage.MissingText) }
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
