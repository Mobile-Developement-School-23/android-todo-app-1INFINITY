package ru.mirea.ivashechkinav.todo

import android.app.Application
import android.util.Log
import androidx.work.*
import ru.mirea.ivashechkinav.todo.data.workmanager.MyWorkerFactory
import ru.mirea.ivashechkinav.todo.data.workmanager.RepeatRequestWorker
import ru.mirea.ivashechkinav.todo.di.components.AppComponent
import ru.mirea.ivashechkinav.todo.di.components.DaggerAppComponent
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.receiver.NetworkChangeReceiver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class App : Application(), Configuration.Provider {
    @Inject
    lateinit var repository: TodoItemsRepository

    lateinit var networkChangeReceiver: NetworkChangeReceiver

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().create(appContext = this.applicationContext)
        appComponent.inject(this)
        networkChangeReceiver = NetworkChangeReceiver(this)
        schedule()
    }

    private fun schedule() {
        // Worker
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val periodicRefreshRequest = PeriodicWorkRequest.Builder(
            RepeatRequestWorker::class.java,
            8,
            TimeUnit.HOURS,
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RepeatRequestWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRefreshRequest
        )
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(MyWorkerFactory(repository))
            .setMinimumLoggingLevel(Log.VERBOSE)
            .build()
    }
}
