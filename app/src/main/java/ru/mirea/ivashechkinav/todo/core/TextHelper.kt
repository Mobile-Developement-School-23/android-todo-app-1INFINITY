package ru.mirea.ivashechkinav.todo.core

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import ru.mirea.ivashechkinav.todo.di.components.AppContext
import javax.inject.Inject

class TextHelper @Inject constructor(@AppContext private val applicationContext: Context){
    fun getString(@StringRes resId: Int): String {
        return applicationContext.getString(resId)
    }
    fun getString(@PluralsRes resId: Int, quantity: Int, number: Int): String {
        return applicationContext.resources.getQuantityString(resId, quantity, number)
    }
}
