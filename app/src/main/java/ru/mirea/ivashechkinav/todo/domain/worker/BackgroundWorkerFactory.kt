package ru.mirea.ivashechkinav.todo.domain.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject

class BackgroundWorkerFactory @Inject constructor(
    private val repeatRequestWorker: RepeatRequestWorker.Factory,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            RepeatRequestWorker::class.java.name ->
                repeatRequestWorker.create(appContext, workerParameters)
            else -> null
        }
    }
}