package ru.mirea.ivashechkinav.todo.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import java.sql.Timestamp


@Dao
interface TodoDao {

    @Query("SELECT * FROM todoItems")
    fun getAll(): List<TodoItem>

    @Query("SELECT * FROM todoItems")
    fun getAllFlow(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todoItems WHERE isComplete = 0")
    fun getAllFlowUnchecked(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todoItems WHERE id = :itemId")
    suspend fun getById(itemId: String): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<TodoItem>)

    @Transaction
    suspend fun upsertItem(item: TodoItem) {
        val localItem = getById(item.id)
        if (localItem != null) {
            if (localItem.changeTimestamp < item.changeTimestamp) {
                update(localItem)
                return
            }
        }
        save(item)
    }

    @Update
    suspend fun update(item: TodoItem)

    @Query("UPDATE todoItems SET isComplete = NOT isComplete, changeTimestamp = :timestamp WHERE id = :itemId")
    suspend fun toggleItemCompleteState(itemId: String, timestamp: Long)

    @Query("DELETE FROM todoItems WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("SELECT COUNT(*) FROM todoItems WHERE isComplete = 1")
    fun getCompletedCount(): Int

}