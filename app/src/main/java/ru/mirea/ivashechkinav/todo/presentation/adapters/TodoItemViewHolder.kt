package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StrikethroughSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import kotlin.math.roundToInt

class TodoItemViewHolder(itemView: View, private val applicationContext: Context) : RecyclerView.ViewHolder(itemView) {
    val root = itemView
    var todoItem: TodoItem? = null
    val isCompleteCheckBox: CheckBox = itemView.findViewById(R.id.cbIsComplete)
    private val todoText: TextView = itemView.findViewById(R.id.tvTodoText)

    fun onBind(todoItem: TodoItem) {
        this.todoItem = todoItem
        isCompleteCheckBox.isChecked = todoItem.isComplete
        todoText.text = todoItem.text
        initState()
    }

    private fun initState() {
        val item = todoItem ?: return
        initCheckedState(item)
        initImportanceState(item)
    }

    private fun initCheckedState(item: TodoItem) {
        if (item.isComplete) {
            val s = SpannableString(item.text)
            val foregroundColorSpan = ForegroundColorSpan(ContextCompat.getColor(applicationContext, R.color.label_light_tertiary))
            val strikethroughSpan = StrikethroughSpan()
            s.setSpan(foregroundColorSpan, 0, s.length, 0)
            s.setSpan(strikethroughSpan, 0, s.length, 0)
            todoText.text = s
            return
        }
        todoText.text = item.text
    }

    private fun initImportanceState(item: TodoItem) {
        when (item.importance) {
            Importance.LOW -> {
                todoText.text = getTextWithIcon(item.text, R.drawable.ic_importance_low)
            }
            Importance.COMMON -> {
                todoText.text = item.text
            }
            Importance.HIGH -> {
                todoText.text = getTextWithIcon(item.text, R.drawable.ic_importance_high)
            }
            else -> throw UnsupportedOperationException("Unknown Importance value: ${item.importance}")
        }
    }
    private fun getTextWithIcon(text: String, @DrawableRes id: Int): SpannableString {
        val drawable = ContextCompat.getDrawable(applicationContext, id) ?: return SpannableString(text)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
        val s = SpannableString(text)

        s.setSpan(imageSpan, 0, 1,  0)
        return s
    }
}