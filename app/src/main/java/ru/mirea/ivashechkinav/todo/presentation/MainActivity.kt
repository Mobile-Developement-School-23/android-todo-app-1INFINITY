package ru.mirea.ivashechkinav.todo.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.ActivityMainBinding
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var todoRecyclerView: RecyclerView
    private val todoItemsRepository = TodoItemsRepositoryImpl()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todoRecyclerView = binding.rwTodoList
        val todoAdapter = TodoAdapter()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = layoutManager
        todoAdapter.todoItems = todoItemsRepository.getAllItems()
    }
}