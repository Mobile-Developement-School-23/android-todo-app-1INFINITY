package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
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
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.EffectUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.EventUi
import javax.inject.Inject

class MainFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val vm: MainViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var todoAdapterFactory: TodoAdapter.TodoAdapterFactory
    private lateinit var todoAdapter: TodoAdapter

    private lateinit var  binding : FragmentMainBinding

    private lateinit var todoRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity)
            .activityComponent
            .mainFragmentComponentFactory()
            .create()
            .inject(this)

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
                handleEffect(it)
            }
        }
    }

    private fun handleEffect(effect: EffectUi) {
        when (effect) {
            is EffectUi.ShowSnackbar -> Snackbar.make(
                binding.root,
                effect.message,
                Snackbar.LENGTH_SHORT
            ).show()

            is EffectUi.ToTaskFragmentUpdate -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(taskId = effect.todoItemId)
                findNavController().navigate(action)
            }

            is EffectUi.ToTaskFragmentCreate -> {
                val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate()
                findNavController().navigate(action)
            }

            is EffectUi.ShowSnackbarWithPullRetry -> {
                Snackbar.make(
                    binding.root,
                    getString(R.string.loading_error_message),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.retry_action_text)) {
                    vm.setEvent(EventUi.OnSnackBarPullRetryButtonClicked)
                }.show()
            }
        }
    }

    private fun initVisibleButton() {
        binding.cbVisible.setOnCheckedChangeListener { _, isChecked ->
            vm.setEvent(
                EventUi.OnVisibleChange(isFilterCompleted = isChecked)
            )
        }
    }

    private fun initRecyclerView() {
        todoRecyclerView = binding.rwTodoList
        todoAdapter = todoAdapterFactory.create(
            object : TodoAdapter.Listener {
                override fun onItemClicked(itemId: String) {
                    vm.setEvent(EventUi.OnItemSelected(itemId))
                }

                override fun onItemChecked(itemId: String) {
                    vm.setEvent(EventUi.OnItemCheckedChange(itemId))
                }
            }
        )
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
    }

    private fun floatingButtonInit() {
        binding.floatingActionButton.setOnClickListener {
            vm.setEvent(
                EventUi.OnFloatingButtonClick
            )
        }
    }

    private fun initRecyclerViewSwipes() {
        val swipeCallback = SwipeTodoItemCallback(
            onSwipeLeft = { itemId ->
                vm.setEvent(
                    EventUi.OnItemSwipeToDelete(itemId)
                )
            },
            onSwipeRight = { itemId ->
                vm.setEvent(
                    EventUi.OnItemSwipeToCheck(itemId)
                )
            },
            applicationContext = requireActivity().baseContext
        )
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rwTodoList)
    }

}
