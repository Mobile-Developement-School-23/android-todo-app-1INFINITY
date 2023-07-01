package ru.mirea.ivashechkinav.todo.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.mirea.ivashechkinav.todo.data.models.TodoItem

@Database(
    version = 1,
    entities = [
        TodoItem::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getTodoDao(): TodoDao

}