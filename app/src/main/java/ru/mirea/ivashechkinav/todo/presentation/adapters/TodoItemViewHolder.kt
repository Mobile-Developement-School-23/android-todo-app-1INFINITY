package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.content.Context
import android.graphics.Paint
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
import java.text.SimpleDateFormat
import java.util.*

class TodoItemViewHolder(itemView: View, private val applicationContext: Context) :
    RecyclerView.ViewHolder(itemView) {
    val root = itemView
    var todoItem: TodoItem? = null
    val isCompleteCheckBox: CheckBox = itemView.findViewById(R.id.cbIsComplete)
    private val todoText: TextView = itemView.findViewById(R.id.tvTodoText)
    private val importanceIcon: ImageView = itemView.findViewById(R.id.imImportance)
    private val deadlineText: TextView = itemView.findViewById(R.id.tvDeadlineText)

    fun onBind(todoItem: TodoItem) {
        this.todoItem = todoItem
        todoText.text = todoItem.text
        initState()
    }

    private fun initState() {
        val item = todoItem ?: return
        initCheckedState(item)
        initImportanceState(item)
        initDeadlineDate(item)
    }

    private fun initDeadlineDate(item: TodoItem) {
        val date = item.deadlineTimestamp
        if (item.isComplete || date == null) {
            deadlineText.visibility = View.GONE
            return
        }
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        deadlineText.text = dateFormat.format(date)
        deadlineText.visibility = View.VISIBLE
    }

    private fun initCheckedState(item: TodoItem) {
        isCompleteCheckBox.isChecked = item.isComplete
        if (item.isComplete) {
            setCheckedCheckBoxState()
            return
        }
        todoText.setTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.label_primary
            )
        )
        todoText.paintFlags = todoText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        if (item.importance == Importance.HIGH) {
            setImportantCheckBoxState()
        } else {
            setCommonCheckBoxState()
        }
    }
    private fun setCheckedCheckBoxState() {
        todoText.paintFlags = todoText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        todoText.setTextColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.label_tertiary
            )
        )

        isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_checked)
        isCompleteCheckBox.buttonTintList =
            AppCompatResources.getColorStateList(applicationContext, R.color.color_green)
    }
    private fun setImportantCheckBoxState() {
        isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_unchecked_high)
        isCompleteCheckBox.buttonTintList =
            AppCompatResources.getColorStateList(applicationContext, R.color.color_red)
    }
    private fun setCommonCheckBoxState() {
        isCompleteCheckBox.setButtonDrawable(R.drawable.checkbox_unchecked_normal)
        isCompleteCheckBox.buttonTintList = AppCompatResources.getColorStateList(
            applicationContext,
            R.color.support_separator
        )
    }

    private fun initImportanceState(item: TodoItem) {
        when (item.importance) {
            Importance.LOW -> setLowViewState(item)
            Importance.COMMON -> setCommonViewState(item)
            Importance.HIGH -> setHighViewState(item)
            else -> throw UnsupportedOperationException("Unknown Importance value: ${item.importance}")
        }
    }
    fun setLowViewState(item: TodoItem) {
        if (item.isComplete) {
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
    fun setCommonViewState(item: TodoItem) {
        importanceIcon.visibility = View.GONE
    }
    fun setHighViewState(item: TodoItem) {
        if (item.isComplete) {
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
}