package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import retrofit2.HttpException
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.core.BadRequestException
import ru.mirea.ivashechkinav.todo.core.DuplicateItemException
import ru.mirea.ivashechkinav.todo.core.NetworkException
import ru.mirea.ivashechkinav.todo.core.OperationRepeatHandler
import ru.mirea.ivashechkinav.todo.core.ServerSideException
import ru.mirea.ivashechkinav.todo.core.TextHelper
import ru.mirea.ivashechkinav.todo.core.TodoItemNotFoundException
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.UiEffect
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.UiState
import ru.mirea.ivashechkinav.todo.presentation.receiver.NetworkChangeReceiver
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val repository: TodoItemsRepository,
    private val networkChangeReceiver: NetworkChangeReceiver,
    private val textHelper: TextHelper,
    private val handler: OperationRepeatHandler,
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }
    private val scope = viewModelScope + exceptionHandler

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _effect: Channel<UiEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    private fun setState(reduce: UiState.() -> UiState) {
        val newState = uiState.value.reduce()
        _uiState.value = newState
    }

    private fun setEffect(builder: () -> UiEffect) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }

    private val visibleStateFlow = MutableStateFlow(false)
    private val itemsFlow = visibleStateFlow.flatMapLatest {
        repository.getTodoItemsByCheckedState(isChecked = it)
    }

    init {
        scope.launch {
            syncItems()
            networkChangeReceiver.stateFlow.collectLatest { isConnected ->
                handleConnectChange(isConnected)
            }
        }
        scope.launch {
            itemsFlow.collect { handleItems(it) }
        }
    }

    private fun handleConnectChange(isConnected: Boolean) {
        if (!isConnected) {
            setEffect { UiEffect.ShowSnackbar(textHelper.getString(R.string.connection_lost_message)) }
        } else {
            setEffect { UiEffect.ShowSnackbar(textHelper.getString(R.string.connection_established_message)) }
            syncItems()
        }
    }

    private suspend fun handleItems(list: List<TodoItem>) {
        val count = repository.getCountOfCompletedItems()
        if (uiState.value.isFilterCompleted) {
            setState {
                copy(
                    countOfCompletedText = textHelper.getString(
                        R.plurals.textCountHiddenItems,
                        0,
                        count
                    ),
                    todoItems = list.filter { !it.isComplete }
                )
            }
        } else {
            setState {
                copy(
                    countOfCompletedText = textHelper.getString(
                        R.plurals.textCountCompletedItems,
                        0,
                        count
                    ),
                    todoItems = list
                )
            }
        }
    }

    fun changeVisibilityState(isFilterCompleted: Boolean) = scope.launch {
        visibleStateFlow.value = isFilterCompleted
        setState { copy(isFilterCompleted = isFilterCompleted) }
    }

    fun selectItem(itemId: String) = scope.launch {
        setEffect { UiEffect.ToTaskFragmentUpdate(itemId) }
    }

    fun toggleCheckItem(itemId: String) = scope.launch {
        val currentTimestamp = System.currentTimeMillis() / 1000
        handler.retryWithAttempts {
            repository.toggleItemCheckedState(itemId, currentTimestamp)
        }
    }

    fun deleteItem(itemId: String) = scope.launch {
        handler.retryWithAttempts { repository.deleteItemById(itemId) }
    }

    fun floatingButtonClick() = setEffect { UiEffect.ToTaskFragmentCreate }

    fun settingsButtonClick() = setEffect { UiEffect.ToSettingsFragment }

    fun syncItems() = scope.launch {
        val syncResult = handler.retryWithAttempts { repository.syncItems() }
        if (syncResult is ResultData.Failure) {
            setEffect { UiEffect.ShowSnackbarWithPullRetry }
        }
    }

    private fun handleException(e: Throwable) {
        val errorText =
            when (e) {
                is HttpException, is NetworkException -> textHelper.getString(R.string.connection_missing_message)

                is ServerSideException,
                is BadRequestException,
                is TodoItemNotFoundException,
                is DuplicateItemException
                -> textHelper.getString(R.string.server_error_message)

                else -> textHelper.getString(R.string.unknown_error_message)
            }
        setEffect { UiEffect.ShowSnackbar(message = errorText) }
    }
}
