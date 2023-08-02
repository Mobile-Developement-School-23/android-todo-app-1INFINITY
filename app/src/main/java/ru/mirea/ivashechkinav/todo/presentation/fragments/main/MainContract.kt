package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class MainContract {

    sealed class UiEffect {
        data class ShowSnackbar(val message: SnackbarMessage) : UiEffect()
        data class ToTaskFragmentUpdate(val todoItemId: String) : UiEffect()
        object ToTaskFragmentCreate : UiEffect()
        object ShowSnackbarWithPullRetry : UiEffect()
        object ToSettingsFragment : UiEffect()
    }

    data class UiState(
        val todoItems: List<TodoItem> = listOf(),
        val isHiddenCompleted: Boolean = false,
        val countOfCompleted: Int = 0
    )

    enum class SnackbarMessage {
        ConnectionLost,
        ConnectionRestored,
        ConnectionMissing,
        ServerError,
        UnknownError,
    }
}