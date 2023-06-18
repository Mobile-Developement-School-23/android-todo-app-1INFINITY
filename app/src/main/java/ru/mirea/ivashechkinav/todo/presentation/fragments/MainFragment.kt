package ru.mirea.ivashechkinav.todo.presentation.fragments

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
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var repository: TodoItemsRepository
    private lateinit var todoAdapter: TodoAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        repository = (requireActivity().application as App).repository
        recyclerViewInit()
        floatingButtonInit()
        initTodoListObserve()
        return binding.root
    }

    private fun initTodoListObserve() {
        lifecycleScope.launch {
            repository.getTodoItemsFlow().collect {
                todoAdapter.submitList(it)
            }
        }
    }

    private fun recyclerViewInit() {
        todoRecyclerView = binding.rwTodoList
        todoAdapter = TodoAdapter(
            object : TodoAdapter.Listener {
                override fun onItemClicked(todoItem: TodoItem) {
                    val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(
                        taskId = todoItem.id
                    )
                    findNavController().navigate(action)
                }

                override fun onItemChecked(todoItem: TodoItem) {
                    val itemChecked = todoItem
                        .copy(isComplete = !todoItem.isComplete)
                    lifecycleScope.launch {
                        repository.updateItem(itemChecked)
                    }
                }
            },
            applicationContext = activity!!.applicationContext
        )
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
        todoAdapter.submitList(repository.getAllItems())
        initRecyclerViewSwipes(todoAdapter)
    }

    private fun floatingButtonInit() {
        val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate()
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerViewSwipes(adapter: TodoAdapter) {
        val swipeCallback = SwipeTodoItemCallback(
            onSwipeLeft = { position ->
                val itemId = adapter.getItemAtPosition(position).id
                lifecycleScope.launch {
                    repository.deleteItemById(itemId)
                }
            },
            onSwipeRight = { position ->
                val oldItem = adapter
                    .getItemAtPosition(position)
                val itemChecked = oldItem
                    .copy(isComplete = !oldItem.isComplete)
                lifecycleScope.launch {
                    repository.updateItem(itemChecked)
                }
            },
            applicationContext = activity!!.baseContext
        )

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rwTodoList)
    }
}