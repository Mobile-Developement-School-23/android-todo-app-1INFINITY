package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class MainContract {
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
}