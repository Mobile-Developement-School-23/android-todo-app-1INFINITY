package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.di.components.MainFragmentViewScope
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.adapters.RoundedItemDecorator
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskViewModel
import javax.inject.Inject

@MainFragmentViewScope
class MainViewController @Inject constructor(
    private val activity: MainActivity,
    private val adapter: TodoAdapter,
    private val navController: NavController,
    private val binding: FragmentMainBinding,
    private val lifecycleOwner: LifecycleOwner,
    fragment: MainFragment,
    vmFactory: ViewModelProvider.Factory,
    ) {
    private val vm: MainViewModel by lazy{
        ViewModelProvider(fragment, vmFactory)[MainViewModel::class.java]
    }
    init {
        initSettingsButton()
        initVisibleButton()
        initRecyclerView()
        initRecyclerViewSwipes()
        initFloatingButton()
        initViewModelObservers()
    }

    private fun initSettingsButton() {
        binding.btnOpenSettings.setOnClickListener {
            vm.settingsButtonClick()
        }
    }

    private fun initViewModelObservers() {
        lifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                val textId =
                    if (state.isHiddenCompleted) R.plurals.textCountHiddenItems
                    else R.plurals.textCountCompletedItems
                binding.tvCountDone.text = activity.resources.getQuantityString(
                    textId,
                    0,
                    state.countOfCompleted
                )
                adapter.submitList(state.todoItems)
            }
        }
        lifecycleOwner.lifecycleScope.launch {
            vm.effect.collect {
                handleEffect(it)
            }
        }
    }

    private fun handleEffect(effect: MainContract.UiEffect) {
        when (effect) {
            is MainContract.UiEffect.ShowSnackbar -> Snackbar.make(
                binding.root,
                parseSnackbarMessages(effect.message),
                Snackbar.LENGTH_SHORT
            ).show()

            is MainContract.UiEffect.ToTaskFragmentUpdate -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(taskId = effect.todoItemId)
                navController.navigate(action)
            }

            is MainContract.UiEffect.ToTaskFragmentCreate -> {
                val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate()
                navController.navigate(action)
            }

            is MainContract.UiEffect.ShowSnackbarWithPullRetry -> {
                Snackbar.make(
                    binding.root,
                    activity.getString(R.string.loading_error_message),
                    Snackbar.LENGTH_LONG
                ).setAction(activity.getString(R.string.retry_action_text)) {
                    vm.syncItems()
                }.show()
            }

            is MainContract.UiEffect.ToSettingsFragment -> {
                navController.navigate(
                    MainFragmentDirections.actionMainFragmentToSettingsFragment()
                )
            }
        }
    }

    private fun initVisibleButton() {
        binding.cbVisible.setOnCheckedChangeListener { _, isChecked ->
            vm.changeVisibilityState(isFilterCompleted = isChecked)
        }

    }

    private fun initRecyclerView() {
        val todoRecyclerView = binding.rwTodoList
        val layoutManager =
            LinearLayoutManager(activity.baseContext, LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = layoutManager
        todoRecyclerView.addItemDecoration(
            RoundedItemDecorator()
        )
    }

    private fun initFloatingButton() {
        binding.floatingActionButton.setOnClickListener {
            vm.floatingButtonClick()
        }
    }

    private fun initRecyclerViewSwipes() {
        val swipeCallback = SwipeTodoItemCallback(
            onSwipeLeft = { itemId ->
                vm.deleteItem(itemId)
            },
            onSwipeRight = { itemId ->
                vm.toggleCheckItem(itemId)
            },
            context = activity.baseContext
        )
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rwTodoList)
    }

    private fun parseSnackbarMessages(message: MainContract.SnackbarMessage): String {
        val resId = when (message) {
            MainContract.SnackbarMessage.ConnectionRestored -> R.string.connection_restored_message
            MainContract.SnackbarMessage.ConnectionLost -> R.string.connection_lost_message
            MainContract.SnackbarMessage.UnknownError -> R.string.unknown_error_message
            MainContract.SnackbarMessage.ServerError -> R.string.server_error_message
            MainContract.SnackbarMessage.ConnectionMissing -> R.string.connection_missing_message
        }
        return activity.getString(resId)
    }
}