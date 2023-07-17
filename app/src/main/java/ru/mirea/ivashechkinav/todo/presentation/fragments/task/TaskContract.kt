package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import ru.mirea.ivashechkinav.todo.data.models.Importance

class TaskContract {
    sealed class UiEvent {
        object OnCancelButtonClicked : UiEvent()
        object OnSaveButtonClicked : UiEvent()
        object OnDeleteButtonClicked : UiEvent()
        object OnBackButtonClicked : UiEvent()
        data class OnTodoTextEdited(val editedText: String) : UiEvent()
        data class OnImportanceSelected(val importance: Importance) : UiEvent()
        data class OnDeadlineSelected(val timestamp: Long) : UiEvent()
        data class OnDeadlineSwitchChanged(val isChecked: Boolean) : UiEvent()
        data class OnTodoItemIdLoaded(val todoItemId: String) : UiEvent()
    }

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