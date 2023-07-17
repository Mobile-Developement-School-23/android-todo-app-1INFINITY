package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.di.components.ActivityScope
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewModel

class TodoAdapter @AssistedInject constructor(
    @Assisted private val viewModel: MainViewModel,
) : ListAdapter<TodoItem, TodoItemViewHolder>(DiffCallback()) {

    private fun onCheckClicked(itemId: String) =
        viewModel.setEvent(MainContract.UiEvent.OnItemCheckedChange(itemId))

    private fun onRootClicked(itemId: String) =
        viewModel.setEvent(MainContract.UiEvent.OnItemSelected(itemId))

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
            setItemBackground(position)
            onBind(item)
            root.setOnClickListener { onRootClicked(item.id) }
            isCompleteCheckBox.setOnClickListener { onCheckClicked(item.id) }
        }
    }

    private fun TodoItemViewHolder.setItemBackground(itemPosition: Int) {
        val maxPosition = currentList.size - 1
        when (itemPosition) {
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
        fun create(viewModel: MainViewModel): TodoAdapter
    }
}
