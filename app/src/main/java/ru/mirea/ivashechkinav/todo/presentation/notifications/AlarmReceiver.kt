package ru.mirea.ivashechkinav.todo.presentation.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.room.TodoDao
import java.time.LocalDate
import javax.inject.Inject

class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var todoDao: TodoDao

    override fun onReceive(context: Context, intent: Intent) {
        (context as App).appComponent.inject(this)

        CoroutineScope(Dispatchers.IO).launch {
            val items = getFilteredItems()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            items.forEach { todo ->
                val importance = when(todo.importance){
                    Importance.LOW -> "Неважно"
                    Importance.COMMON -> "Немного важно"
                    Importance.HIGH -> "Очень важно"
                }
                val notification = NotificationCompat.Builder(context, "SuperTodoApplicationChannelId")
                    .setContentTitle("Запланировано")
                    .setContentText("${importance}: ${todo.text}")
                    .build()
                notificationManager.notify(todo.id.hashCode(), notification)
            }
        }
    }
    private fun getFilteredItems(): List<TodoItem> {
        val dateNow = LocalDate.now()
        return todoDao.getAll()
            .filter {
                val deadline = it.deadlineTimestamp ?: return@filter false
                if(isNotTodoToday(deadline, dateNow)) return@filter false
                return@filter !it.isComplete
            }
    }
    private fun isNotTodoToday(timestamp: Long, dateNow: LocalDate): Boolean {
        val date = LocalDate.ofEpochDay(timestamp / (24 * 60 * 60 * 1000))
        return !date.isEqual(dateNow)
    }
}