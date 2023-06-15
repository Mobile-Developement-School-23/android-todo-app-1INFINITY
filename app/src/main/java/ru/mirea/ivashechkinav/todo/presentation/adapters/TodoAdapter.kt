package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class TodoAdapter(
    private val listener: Listener,
    private val applicationContext: Context
) : RecyclerView.Adapter<TodoItemViewHolder>(), View.OnClickListener {

    private val differ: AsyncListDiffer<TodoItem> = AsyncListDiffer(this, DiffCallback())

    fun submitList(list: List<TodoItem>) = differ.submitList(list)

    fun currentList(): List<TodoItem> = differ.currentList

    fun getItemAtPosition(position: Int): TodoItem{
        return currentList()[position]
    }

    interface Listener {
        fun onItemClicked(todoItem: TodoItem)
        fun onItemChecked(todoItem: TodoItem)
    }

    override fun onClick(v: View) {
        val itemPos = v.tag as Int
        val todoItem = currentList()[itemPos]
        when(v.id){
            R.id.cbIsComplete -> listener.onItemChecked(todoItem)
            else -> listener.onItemClicked(todoItem)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val vh = TodoItemViewHolder(
            layoutInflater.inflate(
                R.layout.item_container_todo,
                parent,
                false
            ),
            applicationContext
        )
        vh.root.setOnClickListener(this)
        return vh
    }

    override fun getItemCount() = currentList().size

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        holder.onBind(currentList()[position])
        holder.root.tag = position
        holder.isCompleteCheckBox.tag = position
    }

    private class DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TodoItem, newItem: TodoItem) =
            oldItem == newItem
    }

}