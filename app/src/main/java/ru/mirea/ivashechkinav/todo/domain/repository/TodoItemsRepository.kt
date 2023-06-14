package ru.mirea.ivashechkinav.todo.domain.repository

import ru.mirea.ivashechkinav.todo.data.models.TodoItem

interface TodoItemsRepository {

    fun addItem(item: TodoItem): Boolean

    fun deleteItemById(id: String): Boolean

    fun updateItem(item: TodoItem): Boolean

    fun getAllItems(): List<TodoItem>

    fun getItemById(id: String): TodoItem?
}