package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter

class MainFragment : Fragment() {
    private val vm: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: FragmentMainBinding
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        initVisibleButton()
        initRecyclerView()
        initRecyclerViewSwipes()
        floatingButtonInit()
        initViewModelObservers()
        return binding.root
    }

    private fun initViewModelObservers() {
        lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                binding.tvCountDone.text = state.countOfCompletedText
                todoAdapter.submitList(state.todoItems)
            }
        }
        lifecycleScope.launch {
            vm.effect.collect {
                when (it) {
                    is MainViewModel.EffectUi.ShowSnackbar -> {
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_SHORT).show()
                    }
                    is MainViewModel.EffectUi.ToTaskFragmentUpdate -> {
                        val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(
                            taskId = it.todoItemId
                        )
                        findNavController().navigate(action)
                    }
                    is MainViewModel.EffectUi.ToTaskFragmentCreate -> {
                        val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate()
                        findNavController().navigate(action)
                    }
                    is MainViewModel.EffectUi.ShowSnackbarWithPullRetry -> {
                        val mySnackbar = Snackbar.make(binding.root, "Произошла ошибка при загрузке списка из интернета", Snackbar.LENGTH_LONG)
                        mySnackbar.setAction("Повторить") {
                            vm.setEvent(MainViewModel.EventUi.OnSnackBarPullRetryButtonClicked)
                        }
                        mySnackbar.show()
                    }
                }
            }
        }
    }

    private fun initVisibleButton() {
        binding.cbVisible.setOnCheckedChangeListener { _, isChecked ->
            vm.setEvent(
                MainViewModel.EventUi.OnVisibleChange(isFilterCompleted = isChecked)
            )
        }
    }

    private fun initRecyclerView() {
        todoRecyclerView = binding.rwTodoList
        todoAdapter = TodoAdapter(
            object : TodoAdapter.Listener {
                override fun onItemClicked(todoItem: TodoItem) {
                    vm.setEvent(
                        MainViewModel.EventUi.OnItemSelected(todoItem)
                    )
                }

                override fun onItemChecked(todoItem: TodoItem) {
                    vm.setEvent(
                        MainViewModel.EventUi.OnItemCheckedChange(todoItem)
                    )
                }
            },
            applicationContext = activity!!.applicationContext
        )
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
    }

    private fun floatingButtonInit() {
        binding.floatingActionButton.setOnClickListener {
            vm.setEvent(
                MainViewModel.EventUi.OnFloatingButtonClick
            )
        }
    }

    private fun initRecyclerViewSwipes() {
        val swipeCallback = SwipeTodoItemCallback(
            onSwipeLeft = { todoItem ->
                vm.setEvent(
                    MainViewModel.EventUi.OnItemSwipeToDelete(todoItem)
                )
            },
            onSwipeRight = { todoItem ->
                vm.setEvent(
                    MainViewModel.EventUi.OnItemSwipeToCheck(todoItem)
                )
            },
            applicationContext = activity!!.baseContext
        )

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rwTodoList)
    }
}