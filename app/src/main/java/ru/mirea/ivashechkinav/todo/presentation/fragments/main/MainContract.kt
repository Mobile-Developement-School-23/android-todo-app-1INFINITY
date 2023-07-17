package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class MainContract {
    sealed class UiEvent {
        data class OnVisibleChange(val isFilterCompleted: Boolean) : UiEvent()
        data class OnItemSelected(val itemId: String) : UiEvent()
        data class OnItemCheckedChange(val itemId: String) : UiEvent()
        data class OnItemSwipeToDelete(val itemId: String) : UiEvent()
        data class OnItemSwipeToCheck(val itemId: String) : UiEvent()
        object OnFloatingButtonClick : UiEvent()
        object OnSnackBarPullRetryButtonClicked : UiEvent()
        object OnSettingsButtonClick : UiEvent()
    }

    sealed class UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect()
        data class ToTaskFragmentUpdate(val todoItemId: String) : UiEffect()
        object ToTaskFragmentCreate : UiEffect()
        object ShowSnackbarWithPullRetry : UiEffect()
        object ToSettingsFragment : UiEffect()
    }

    data class UiState(
        val countOfCompletedText: String = "Загрузка выполненных задач...",
        val todoItems: List<TodoItem> = listOf(),
        val isFilterCompleted: Boolean = false
    )
}