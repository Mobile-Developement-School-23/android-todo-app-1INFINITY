package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import ru.mirea.ivashechkinav.todo.data.models.Importance

class TaskContract {
    sealed class FragmentViewState {
        object Loading : FragmentViewState()
        object Update : FragmentViewState()
    }

    sealed class UiEffect {
        data class ShowSnackbar(val message: String) : UiEffect()
        object ToBackFragment : UiEffect()
        object ShowDatePicker : UiEffect()
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
}