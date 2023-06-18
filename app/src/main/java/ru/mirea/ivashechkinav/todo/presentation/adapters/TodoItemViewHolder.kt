package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

class TodoItemViewHolder(itemView: View, private val applicationContext: Context) :
    RecyclerView.ViewHolder(itemView) {
    val root = itemView
    var todoItem: TodoItem? = null
    val isCompleteCheckBox: CheckBox = itemView.findViewById(R.id.cbIsComplete)
    private val todoText: TextView = itemView.findViewById(R.id.tvTodoText)
    private val importanceIcon: ImageView = itemView.findViewById(R.id.imImportance)

    fun onBind(todoItem: TodoItem) {
        this.todoItem = todoItem
        todoText.text = todoItem.text
        initState()
    }

    private fun initState() {
        val item = todoItem ?: return
        initCheckedState(item)
        initImportanceState(item)
    }

    private fun initCheckedState(item: TodoItem) {
        isCompleteCheckBox.isChecked = item.isComplete
        if (item.isComplete) {
            todoText.paintFlags = todoText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            todoText.setTextColor(ContextCompat.getColor(applicationContext, R.color.label_light_tertiary))

            isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_checked)
            isCompleteCheckBox.buttonTintList = AppCompatResources.getColorStateList(applicationContext, R.color.color_light_green)
            return
        }
        todoText.setTextColor(ContextCompat.getColor(applicationContext, R.color.label_light_primary))
        todoText.paintFlags = todoText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        if(item.importance == Importance.HIGH) {
            isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_unchecked_high)
            isCompleteCheckBox.buttonTintList = AppCompatResources.getColorStateList(applicationContext, R.color.color_light_red)
        } else {
            isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_unchecked_normal)
            isCompleteCheckBox.buttonTintList = AppCompatResources.getColorStateList(applicationContext, R.color.support_light_separator)
        }
    }

    private fun initImportanceState(item: TodoItem) {
        when (item.importance) {
            Importance.LOW -> {
                if(item.isComplete) {
                    importanceIcon.visibility = View.GONE
                    return
                }
                val drawable = AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_importance_low
                )
                importanceIcon.setImageDrawable(drawable)
                importanceIcon.visibility = View.VISIBLE
            }
            Importance.COMMON -> {
                importanceIcon.visibility = View.GONE
            }
            Importance.HIGH -> {
                if(item.isComplete) {
                    importanceIcon.visibility = View.GONE
                    return
                }
                val drawable = AppCompatResources.getDrawable(
                    applicationContext,
                    R.drawable.ic_importance_high
                )
                importanceIcon.setImageDrawable(drawable)
                importanceIcon.visibility = View.VISIBLE
            }
            else -> throw UnsupportedOperationException("Unknown Importance value: ${item.importance}")
        }
    }
}