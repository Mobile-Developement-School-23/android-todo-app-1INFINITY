package ru.mirea.ivashechkinav.todo.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var todoRecyclerView: RecyclerView
    private val todoItemsRepository = TodoItemsRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        recyclerViewInit()
        floatingButtonInit()
        return binding.root
    }

    private fun recyclerViewInit() {
        todoRecyclerView = binding.rwTodoList
        val todoAdapter = TodoAdapter(object : TodoAdapter.Listener {
            override fun onItemClicked(todoItem: TodoItem) {
                val action = MainFragmentDirections.actionMainFragmentToTaskFragmentCreate(
                    taskId = todoItem.id
                )
                findNavController().navigate(action)
            }

            override fun onItemChecked(todoItem: TodoItem) {
                val itemChecked = todoItem
                    .copy(isComplete = !todoItem.isComplete)
                todoItemsRepository.updateItem(itemChecked)
            }
        },
        applicationContext = activity!!.applicationContext)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
        todoAdapter.submitList(todoItemsRepository.getAllItems())
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
                todoItemsRepository.deleteItemById(itemId)
                adapter.submitList(todoItemsRepository.getAllItems())
            },
            onSwipeRight = { position ->
                val itemChecked = adapter
                    .getItemAtPosition(position)
                    .copy(isComplete = true)
                todoItemsRepository.updateItem(itemChecked)
                adapter.submitList(todoItemsRepository.getAllItems())
            },
            applicationContext = activity!!.baseContext
        )

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rwTodoList)
    }
}