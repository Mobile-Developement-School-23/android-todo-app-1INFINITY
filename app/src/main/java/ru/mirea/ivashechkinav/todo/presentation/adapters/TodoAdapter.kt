package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class TodoAdapter(
    private val listener: Listener,
) : RecyclerView.Adapter<TodoItemViewHolder>(), View.OnClickListener {

    var todoItems = listOf<TodoItem>()

    interface Listener {
        fun OnItemClicked(todoItem: TodoItem)
    }

    override fun onClick(v: View) {
        val itemPos = v.tag as Int
        val todoItem = todoItems[itemPos]
        when(v.id){
            R.id.tvTodoText -> listener.OnItemClicked(todoItem)
            else -> listener.OnItemClicked(todoItem)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val vh = TodoItemViewHolder(
            layoutInflater.inflate(
                R.layout.item_container_todo,
                parent,
                false
            )
        )
        vh.root.setOnClickListener(this)
        vh.todoText.setOnClickListener(this)
        return vh
    }

    override fun getItemCount() = todoItems.size

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        holder.onBind(todoItems[position])
        holder.root.tag = position
        holder.todoText.tag = position
    }


}