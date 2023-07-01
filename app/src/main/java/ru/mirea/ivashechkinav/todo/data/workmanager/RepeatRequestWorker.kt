package ru.mirea.ivashechkinav.todo.data.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class RepeatRequestWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val repository: TodoItemsRepository,
    ) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return when (repository.pullItemsFromServer()) {
            is ResultData.Success -> Result.success()
            is ResultData.Failure -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "ScheduleRepeatRequestWorker"
    }
}

class MyWorkerFactory(private val repository: TodoItemsRepository) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RepeatRequestWorker::class.java.name -> {
                RepeatRequestWorker(appContext, workerParameters, repository)
            }
            else -> null
        }
    }
}