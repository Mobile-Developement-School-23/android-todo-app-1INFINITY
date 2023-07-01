package ru.mirea.ivashechkinav.todo.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.mirea.ivashechkinav.todo.data.models.TodoItem


@Dao
interface TodoDao {

    @Query("SELECT * FROM todoItems")
    fun getAll(): List<TodoItem>

    @Query("SELECT * FROM todoItems")
    fun getAllFlow(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todoItems WHERE isComplete = :isChecked")
    fun getAllFlowWithCheckedState(isChecked: Boolean): Flow<List<TodoItem>>

    @Query("SELECT * FROM todoItems WHERE id = :itemId")
    suspend fun getById(itemId: String): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<TodoItem>)

    @Update
    suspend fun update(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)

    @Query("DELETE FROM todoItems WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("DELETE FROM todoItems")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM todoItems WHERE isComplete = 1")
    fun getCompletedCount(): Int

}