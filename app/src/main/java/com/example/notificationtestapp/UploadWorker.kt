package com.example.notificationtestapp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString(KEY_FILE_ID) ?: return Result.failure()

        return try {
            setProgress(workDataOf(KEY_PROGRESS to 0))

            // Replace with real suspend calls, e.g. api.uploadChunk(...)
            for (step in 1..10) {
                withContext(Dispatchers.IO) { delay(500.milliseconds) }
                Log.e("my_progress","progress : ${step*10}")
                setProgress(workDataOf(KEY_PROGRESS to step * 10))
            }

            Result.success(workDataOf(KEY_RESULT_URL to "https://example.com/files/$fileId"))
        } catch (e: IOException) {
            // Retries use the backoff policy set on the request
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_ID = "file_id"
        const val KEY_PROGRESS = "progress"
        const val KEY_RESULT_URL = "result_url"
    }
}