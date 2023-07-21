package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class MainContract {

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