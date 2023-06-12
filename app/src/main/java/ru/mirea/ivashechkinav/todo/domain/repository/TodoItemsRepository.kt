package ru.mirea.ivashechkinav.todo.domain.repository

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    fun addItem(item: TodoItem): Boolean

    fun getAllItems(): List<TodoItem>
}