package ru.mirea.ivashechkinav.todo

import android.app.Application
import androidx.room.Room
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.data.room.AppDatabase
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class App : Application() {

    private lateinit var todoDao: TodoDao
    lateinit var repository: TodoItemsRepository

    override fun onCreate() {
        super.onCreate()
        todoDao = provideDao()
        repository = TodoItemsRepositoryImpl(todoDao)
    }

    private fun provideDao(): TodoDao {
        return Room.databaseBuilder(
            this.applicationContext,
            AppDatabase::class.java,
            DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build().getTodoDao()
    }

    private companion object {
        const val DB_NAME = "todo.db"
    }
}