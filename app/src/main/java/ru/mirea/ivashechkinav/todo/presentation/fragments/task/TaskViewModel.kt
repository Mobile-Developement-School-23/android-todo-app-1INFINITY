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
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.EffectUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.EventUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.FragmentViewState
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiState
import java.util.UUID
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val repository: TodoItemsRepository, private val textHelper: TextHelper
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }
    private val handler = OperationRepeatHandler(syncAction = { repository.syncItems() })
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
            _event.collect { handleEvent(it) }
        }
    }

    private suspend fun handleEvent(event: EventUi) {
        when (event) {
            is EventUi.OnBackButtonClicked -> setEffect { EffectUi.ToBackFragment }
            is EventUi.OnCancelButtonClicked -> setEffect { EffectUi.ToBackFragment }
            is EventUi.OnSaveButtonClicked -> onSaveButtonClicked()
            is EventUi.OnDeleteButtonClicked -> onDeleteButtonClicked()
            is EventUi.OnTodoTextEdited -> onTodoTextEdited(event)
            is EventUi.OnImportanceSelected -> onImportanceSelected(event)
            is EventUi.OnDeadlineSelected -> onDeadlineSelected(event)
            is EventUi.OnDeadlineSwitchChanged -> onDeadlineSwitchChanged(event)
            is EventUi.OnTodoItemIdLoaded -> onTodoItemIdLoaded(event)
        }
    }

    private suspend fun onSaveButtonClicked() {
        val newItem = validateSaveTask() ?: return

        if (uiState.value.id == null) {
            handler.retryWithAttempts { repository.addItem(newItem) }
        } else {
            handler.retryWithAttempts { repository.updateItem(newItem) }
        }
        setEffect { EffectUi.ToBackFragment }
    }

    private suspend fun onDeleteButtonClicked() {
        val currentTodoItem = uiState.value
        currentTodoItem.id?.let {
            handler.retryWithAttempts { repository.deleteItemById(it) }
        }
        setEffect { EffectUi.ToBackFragment }
    }

    private fun onTodoTextEdited(event: EventUi.OnTodoTextEdited) {
        setState {
            copy(
                text = event.editedText, viewState = FragmentViewState.Update
            )
        }
    }

    private fun onImportanceSelected(event: EventUi.OnImportanceSelected) {
        setState {
            copy(importance = event.importance)
        }
    }

    private fun onDeadlineSelected(event: EventUi.OnDeadlineSelected) {
        setState {
            copy(deadlineTimestamp = event.timestamp)
        }
    }

    private fun onDeadlineSwitchChanged(event: EventUi.OnDeadlineSwitchChanged) {
        if (event.isChecked) {
            setEffect { EffectUi.ShowDatePicker }
            return
        }
        setState {
            copy(deadlineTimestamp = null)
        }
    }

    private suspend fun onTodoItemIdLoaded(event: EventUi.OnTodoItemIdLoaded) { // will it erase state in case of orientation change?
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
        setEffect { EffectUi.ShowSnackbar(message = errorText) }
    }

    private fun validateSaveTask(): TodoItem? {
        val currentTime = System.currentTimeMillis() / SECONDS_DIVIDER
        val currentTodoItem = uiState.value
        if (currentTodoItem.text.isNullOrEmpty()) {
            setEffect { EffectUi.ShowSnackbar(textHelper.getString(R.string.description_needed_message)) }
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
