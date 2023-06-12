package ru.mirea.ivashechkinav.todo.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding
    private val args: TaskFragmentArgs by navArgs()
    private val repository = TodoItemsRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        loadArgs()
        return binding.root
    }
    private fun loadArgs(){
        val todoItem = repository.getItemById(args.taskId ?: return) ?: return
        binding.edTodoItemText.setText(todoItem.text)
        binding.swDeadline.isChecked = todoItem.deadlineTimestamp != null
    }
}