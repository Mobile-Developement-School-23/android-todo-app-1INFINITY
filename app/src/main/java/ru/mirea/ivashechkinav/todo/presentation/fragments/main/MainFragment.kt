package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.UiEffect
import javax.inject.Inject

class MainFragment : Fragment() {

    @Inject
    internal lateinit var vm: MainViewModel

    @Inject
    internal lateinit var todoAdapter: TodoAdapter

    private lateinit var binding: FragmentMainBinding

    private lateinit var todoRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity)
            .activityComponent
            .mainFragmentComponentFactory()
            .create(fragment = this@MainFragment)
            .inject(this@MainFragment)
        initVisibleButton()
        initRecyclerView()
        initRecyclerViewSwipes()
        floatingButtonInit()
        initViewModelObservers()
        binding.btnOpenSettings.setOnClickListener {
            vm.settingsButtonClick()
        }
        return binding.root
    }

    private fun initViewModelObservers() {
        lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                val textId =
                    if (state.isHiddenCompleted) R.plurals.textCountHiddenItems
                    else R.plurals.textCountCompletedItems
                binding.tvCountDone.text = resources.getQuantityString(
                    textId,
                    0,
                    state.countOfCompleted
                )
                todoAdapter.submitList(state.todoItems)
            }
        }
        lifecycleScope.launch {
            vm.effect.collect {
                handleEffect(it)
            }
        }
    }

    private fun handleEffect(effect: UiEffect) {
        when (effect) {
            is UiEffect.ShowSnackbar -> Snackbar.make(
                binding.root,
                parseSnackbarMessages(effect.message),
                Snackbar.LENGTH_SHORT
            ).show()

            is UiEffect.ToTaskFragmentUpdate -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(taskId = effect.todoItemId)
                findNavController().navigate(action)
            }

            is UiEffect.ToTaskFragmentCreate -> {
                val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate()
                findNavController().navigate(action)
            }

            is UiEffect.ShowSnackbarWithPullRetry -> {
                Snackbar.make(
                    binding.root,
                    getString(R.string.loading_error_message),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.retry_action_text)) {
                    vm.syncItems()
                }.show()
            }

            is UiEffect.ToSettingsFragment -> {
                findNavController().navigate(
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
        todoRecyclerView = binding.rwTodoList
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
    }

    private fun floatingButtonInit() {
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
            applicationContext = requireActivity().baseContext
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
        return getString(resId)
    }
}
