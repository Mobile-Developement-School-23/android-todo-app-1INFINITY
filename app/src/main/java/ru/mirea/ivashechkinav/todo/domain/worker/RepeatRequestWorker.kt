package ru.mirea.ivashechkinav.todo.domain.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.mirea.ivashechkinav.todo.domain.repository.ResultData
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository

class RepeatRequestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: TodoItemsRepository,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return when (repository.syncItems()) {
            is ResultData.Success -> Result.success()
            is ResultData.Failure -> Result.retry()
            else -> Result.success()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): RepeatRequestWorker
    }
}