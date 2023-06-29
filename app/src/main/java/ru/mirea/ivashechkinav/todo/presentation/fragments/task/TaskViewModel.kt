package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import java.util.*

class TaskViewModel(repository: TodoItemsRepository) : ViewModel() {
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
        viewModelScope.launch {
            _event.collect { event ->
                when (event) {
                    is EventUi.OnBackButtonClicked -> setEffect { EffectUi.ToBackFragment }
                    is EventUi.OnCancelButtonClicked -> setEffect { EffectUi.ToBackFragment }
                    is EventUi.OnSaveButtonClicked -> {

                        val newItem = validateSaveTask() ?: return@collect

                        if (uiState.value.id == null) {
                            repository.addItem(newItem).CheckFailure()
                        } else {
                            repository.updateItem(newItem).CheckFailure()
                        }
                        setEffect { EffectUi.ToBackFragment }
                    }
                    is EventUi.OnDeleteButtonClicked -> {
                        val currentTodoItem = uiState.value
                        currentTodoItem.id?.let {
                            repository.deleteItemById(it).CheckFailure()
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
                        val result = repository.getItemById(event.todoItemId).also { it.CheckFailure() }
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
                    else -> {
                        throw UnsupportedOperationException("Unknown event class: ${event::class.java.simpleName}")
                    }
                }
            }
        }
    }

    private fun <T> ResultData<T>.CheckFailure() {
        if (this is ResultData.Failure)
            setEffect { EffectUi.ShowSnackbar(this.message) }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = (this[APPLICATION_KEY] as App).repository
                TaskViewModel(
                    repository = repository,
                )
            }
        }
    }
}