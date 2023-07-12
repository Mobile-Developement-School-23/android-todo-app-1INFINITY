package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class MainContract {
    sealed class EventUi {
        data class OnVisibleChange(val isFilterCompleted: Boolean) : EventUi()
        data class OnItemSelected(val itemId: String) : EventUi()
        data class OnItemCheckedChange(val itemId: String) : EventUi()
        data class OnItemSwipeToDelete(val itemId: String) : EventUi()
        data class OnItemSwipeToCheck(val itemId: String) : EventUi()
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