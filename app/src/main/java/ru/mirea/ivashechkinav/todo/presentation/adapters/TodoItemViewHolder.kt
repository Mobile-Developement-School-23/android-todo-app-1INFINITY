package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val root = itemView
    val isCompleteCheckBox: CheckBox = itemView.findViewById(R.id.cbIsComplete)
    val todoText: TextView = itemView.findViewById(R.id.tvTodoText)

    fun onBind(todoItem: TodoItem) {
        isCompleteCheckBox.isChecked = todoItem.isComplete
        todoText.text = todoItem.text
    }
}