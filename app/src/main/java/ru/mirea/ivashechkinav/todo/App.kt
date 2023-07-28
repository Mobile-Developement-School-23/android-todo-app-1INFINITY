package ru.mirea.ivashechkinav.todo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.mirea.ivashechkinav.todo.di.components.AppComponent
import ru.mirea.ivashechkinav.todo.di.components.DaggerAppComponent
import ru.mirea.ivashechkinav.todo.domain.worker.BackgroundWorkerFactory
import ru.mirea.ivashechkinav.todo.domain.worker.RepeatRequestWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class App : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: BackgroundWorkerFactory

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(appContext = this.applicationContext)
        appComponent.inject(this)
        scheduleWorker()
        createChannel()
    }

    private fun scheduleWorker() {
        // Worker
        val syncWorker = PeriodicWorkRequestBuilder<RepeatRequestWorker>(
            8L,
            TimeUnit.HOURS
        ).setConstraints(
            Constraints(
                requiredNetworkType = NetworkType.CONNECTED
            )
        ).build()
        WorkManager.getInstance(this).enqueue(syncWorker)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    "SuperTodoApplicationChannelId",
                    "SuperTodoApplication",
                    NotificationManager.IMPORTANCE_HIGH
                )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
