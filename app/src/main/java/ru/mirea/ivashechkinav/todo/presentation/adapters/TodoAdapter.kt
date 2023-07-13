package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.di.components.ActivityScope
import ru.mirea.ivashechkinav.todo.di.components.AppContext

class TodoAdapter @AssistedInject constructor(
    @Assisted private val listener: Listener, // why not viewmodel?
    @AppContext private val applicationContext: Context
) : ListAdapter<TodoItem, TodoItemViewHolder>(DiffCallback()) { // View.OnClickListener on main class is bad practice
    interface Listener {
        fun onItemClicked(todoItem: TodoItem)
        fun onItemChecked(todoItem: TodoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val vh = TodoItemViewHolder(
            layoutInflater.inflate(
                R.layout.item_container_todo,
                parent,
                false
            ),
            parent.context
        )
        return vh
    }

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.apply {
            setItemBackground(position) // not entirely correct, data changes will break it
            onBind(item)
            root.tag = position // bad practice
            isCompleteCheckBox.tag = position
        }
    }
    private fun TodoItemViewHolder.setItemBackground(itemPosition: Int) {
        val maxPosition = currentList.size - 1
        when(itemPosition) {
            0 -> {
                this.itemView.setBackgroundResource(R.drawable.todo_item_upper_background)
            }
            maxPosition -> {
                this.itemView.setBackgroundResource(R.drawable.todo_item_lower_background)
            }
        }
    }
    private class DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem == newItem
    }
    @ActivityScope
    @AssistedFactory
    interface TodoAdapterFactory {
        fun create(listener: Listener): TodoAdapter
    }
}
