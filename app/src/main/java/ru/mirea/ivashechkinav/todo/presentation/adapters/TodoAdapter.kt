package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewModel
import javax.inject.Inject

class TodoAdapter @Inject constructor(
    private val viewModel: MainViewModel,
) : ListAdapter<TodoItem, TodoItemViewHolder>(DiffCallback()) {

    private fun onCheckClicked(itemId: String) =
        viewModel.toggleCheckItem(itemId)

    private fun onRootClicked(itemId: String) =
        viewModel.selectItem(itemId)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TodoItemViewHolder(
            layoutInflater.inflate(
                R.layout.item_container_todo,
                parent,
                false
            ),
            parent.context
        )
    }

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.apply {
            onBind(item)
            root.setOnClickListener { onRootClicked(item.id) }
            isCompleteCheckBox.setOnClickListener { onCheckClicked(item.id) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem == newItem
    }

}

