package ru.mirea.ivashechkinav.todo.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
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
        val todoAdapter = TodoAdapter()
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
        todoAdapter.todoItems = todoItemsRepository.getAllItems()
    }

    private fun floatingButtonInit() {
        val action = MainFragmentDirections.actionMainFragmentToTaskFragment()
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(action)
        }
    }
}