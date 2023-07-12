package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.EffectUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.EventUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.UiState
import ru.mirea.ivashechkinav.todo.presentation.receiver.NetworkChangeReceiver
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val repository: TodoItemsRepository,
    private val networkChangeReceiver: NetworkChangeReceiver,
    private val textHelper: TextHelper
) : ViewModel() {
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("Coroutine", "Error: ", throwable)
        CoroutineScope(context).launch { handleException(throwable) }
    }
    private val handler = OperationRepeatHandler(
        syncAction = { repository.syncItems() }
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

    private val visibleStateFlow = MutableStateFlow(false)
    private val itemsFlow = visibleStateFlow.flatMapLatest {
        repository.getTodoItemsFlowWith(isChecked = it)
    }

    init {
        viewModelScope.launch(exceptionHandler) {
            syncItems()
            networkChangeReceiver.stateFlow.collectLatest { isConnected ->
                handleConnectChange(isConnected)
            }
        }
        viewModelScope.launch(exceptionHandler) {
            itemsFlow.collect { handleItems(it) }
        }
        viewModelScope.launch(exceptionHandler) {
            _event.collect { handleEvent(it) }
        }
    }

    private suspend fun handleConnectChange(isConnected: Boolean) {
        if (!isConnected) {
            setEffect { EffectUi.ShowSnackbar(textHelper.getString(R.string.connection_lost_message)) }
        } else {
            setEffect { EffectUi.ShowSnackbar(textHelper.getString(R.string.connection_established_message)) }
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

    private suspend fun handleEvent(event: EventUi) {
        when (event) {
            is EventUi.OnVisibleChange -> {
                viewModelScope.launch(exceptionHandler) {
                    visibleStateFlow.value = event.isFilterCompleted
                    setState { copy(isFilterCompleted = event.isFilterCompleted) }
                }
            }

            is EventUi.OnItemSelected -> {
                viewModelScope.launch(exceptionHandler) {
                    val itemId = event.itemId
                    setEffect { EffectUi.ToTaskFragmentUpdate(itemId) }
                }
            }

            is EventUi.OnItemSwipeToCheck -> {
                viewModelScope.launch(exceptionHandler) {
                    val itemId = event.itemId
                    val currentTimestamp = System.currentTimeMillis() / 1000
                    handler.retryWithAttempts {
                        repository.toggleItemCheckedState(
                            itemId,
                            currentTimestamp
                        )
                    }
                }
            }

            is EventUi.OnItemCheckedChange -> {
                viewModelScope.launch(exceptionHandler) {
                    val itemId = event.itemId
                    val currentTimestamp = System.currentTimeMillis() / 1000
                    handler.retryWithAttempts {
                        repository.toggleItemCheckedState(
                            itemId,
                            currentTimestamp
                        )
                    }
                }
            }

            is EventUi.OnItemSwipeToDelete -> {
                viewModelScope.launch(exceptionHandler) {
                    handler.retryWithAttempts { repository.deleteItemById(event.itemId) }
                }
            }

            is EventUi.OnFloatingButtonClick -> {
                setEffect { EffectUi.ToTaskFragmentCreate }
            }

            is EventUi.OnSnackBarPullRetryButtonClicked -> syncItems()
        }
    }

    private suspend fun syncItems() {
        viewModelScope.launch(exceptionHandler) {
            val syncResult = handler.retryWithAttempts { repository.syncItems() }
            if (syncResult is ResultData.Failure) {
                setEffect { EffectUi.ShowSnackbarWithPullRetry }
            }
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
        setEffect { EffectUi.ShowSnackbar(message = errorText) }
    }
}
