package ru.mirea.ivashechkinav.todo

import android.app.Application
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl

class App: Application() {

    val repository = TodoItemsRepositoryImpl()
    override fun onCreate() {
        super.onCreate()
    }
}