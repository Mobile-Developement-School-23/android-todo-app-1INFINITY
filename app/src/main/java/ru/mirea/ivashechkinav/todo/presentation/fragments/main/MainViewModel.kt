package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskViewModel

class MainViewModel(repository: TodoItemsRepository) : ViewModel() {
    sealed class EventUi {
        data class OnVisibleChange(val isFilterCompleted: Boolean): EventUi()
        data class OnItemSelected(val todoItem: TodoItem) : EventUi()
        data class OnItemCheckedChange(val todoItem: TodoItem) : EventUi()
        data class OnItemSwipeToDelete(val todoItem: TodoItem) : EventUi()
        data class OnItemSwipeToCheck(val todoItem: TodoItem) : EventUi()
        object OnFloatingButtonClick : EventUi()
    }

    sealed class EffectUi {
        data class ToTaskFragmentUpdate(val todoItemId: String) : EffectUi()
        object ToTaskFragmentCreate : EffectUi()
    }

    data class UiState(
        val countOfCompletedText: String = "Загрузка выполненных задач...",
        val todoItems: List<TodoItem> = listOf(),
        val isFilterCompleted: Boolean = false
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
            repository.getTodoItemsFlow().collect { list ->
                val count = list.count { it.isComplete }.toString()
                if(uiState.value.isFilterCompleted){
                    setState {
                        copy(
                            countOfCompletedText = "Скрыто выполненных - $count",
                            todoItems = list.filter { !it.isComplete}
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
        viewModelScope.launch {
            _event.collect { event ->
                when (event) {
                    is EventUi.OnVisibleChange -> {
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
                        repository.updateItem(itemChecked)
                    }
                    is EventUi.OnItemCheckedChange -> {
                        val itemChecked = event.todoItem
                            .copy(isComplete = !event.todoItem.isComplete)
                        repository.updateItem(itemChecked)
                    }
                    is EventUi.OnItemSwipeToDelete -> {
                        repository.deleteItemById(event.todoItem.id)
                    }
                    is EventUi.OnFloatingButtonClick -> {
                        setEffect { EffectUi.ToTaskFragmentCreate }
                    }
                    else -> {
                        throw UnsupportedOperationException("Unknown event class: ${event::class.java.simpleName}")
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App).repository
                MainViewModel(
                    repository = repository,
                )
            }
        }
    }
}