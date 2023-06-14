package ru.mirea.ivashechkinav.todo.data.repository

import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class TodoItemsRepositoryImpl: TodoItemsRepository {
    private val todoItems: MutableList<TodoItem> = generateItems()

    override fun addItem(item: TodoItem) = todoItems.add(item)

    override fun deleteItemById(id: String) = todoItems.removeIf { it.id == id }

    override fun updateItem(item: TodoItem): Boolean {
        val itemToUpdate = todoItems.find {it.id == item.id}

        itemToUpdate?.let {
            val updatedItem = it.copy(
                text = item.text,
                importance = item.importance,
                deadlineTimestamp = item.deadlineTimestamp,
                isComplete = item.isComplete,
                changeTimestamp = System.currentTimeMillis()
            )
            val indexToUpdate = todoItems.indexOf(itemToUpdate)
            todoItems[indexToUpdate] = updatedItem
            return true
        }
        return false
    }

    override fun getAllItems() = todoItems.toList()

    override fun getItemById(id: String) = todoItems.firstOrNull { it.id == id }

    private fun generateItems(): MutableList<TodoItem> {
        return mutableListOf(
            TodoItem(
                id = "1",
                text = "Buy groceries:\nMilk\nBread\nEggs\nBeacon",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "2",
                text = "Go for a run",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "3",
                text = "Read a book",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000, // Set deadline for a month later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "4",
                text = "Call mom",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "5",
                text = "Fix leaking faucet",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000, // Set deadline for two days later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "6",
                text = "Attend a meeting",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "7",
                text = "Pay bills",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000, // Set deadline for a month later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "8",
                text = "Write a blog post",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 86400000, // Set deadline for tomorrow
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "9",
                text = "Plan vacation",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000, // Set deadline for a week later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "10",
                text = "Clean the garage",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000, // Set deadline for two days later
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "11",
                text = "Finish the report for the quarterly meeting",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 86400000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "12",
                text = "Prepare a presentation for the project pitch",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 172800000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "13",
                text = "Research and gather data for market analysis",
                importance = Importance.LOW,
                deadlineTimestamp = System.currentTimeMillis() + 2592000000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "14",
                text = "Organize team building activities for the department",
                importance = Importance.HIGH,
                deadlineTimestamp = System.currentTimeMillis() + 432000000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            ),
            TodoItem(
                id = "15",
                text = "Implement new feature based on user feedback and requirements",
                importance = Importance.COMMON,
                deadlineTimestamp = System.currentTimeMillis() + 604800000,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                changeTimestamp = System.currentTimeMillis()
            )
        )
    }
}