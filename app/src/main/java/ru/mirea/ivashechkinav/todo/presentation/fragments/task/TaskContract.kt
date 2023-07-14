package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import ru.mirea.ivashechkinav.todo.data.models.Importance

class TaskContract {
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
}