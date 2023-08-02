package ru.mirea.ivashechkinav.todo.presentation.adapters

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.mirea.ivashechkinav.todo.R

class RoundedItemDecorator: RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val lastPosition = parent.adapter?.itemCount?.minus(1) ?: 0

        if(lastPosition == 0) {
            view.setBackgroundResource(R.drawable.todo_item_rounded_background)
        }
        if (position == 0) {
            view.setBackgroundResource(R.drawable.todo_item_upper_background)
        } else if (position == lastPosition) {
            view.setBackgroundResource(R.drawable.todo_item_lower_background)
        }
    }
}