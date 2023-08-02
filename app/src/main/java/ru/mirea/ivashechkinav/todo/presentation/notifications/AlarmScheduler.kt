package ru.mirea.ivashechkinav.todo.presentation.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ru.mirea.ivashechkinav.todo.di.components.AppContext
import java.util.Calendar
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @AppContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val intent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    fun cancelAlarm() {
        alarmManager.cancel(intent)
    }
    fun scheduleAlarm() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intent
        )
    }
}