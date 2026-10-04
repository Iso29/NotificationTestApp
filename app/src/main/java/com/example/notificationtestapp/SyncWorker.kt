package com.example.notificationtestapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SyncWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {



    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            createChannel(context)
            buildNotification(context,"Work is done")
            // repository.syncNotifications()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        }
    }

    private fun createChannel(
        context: Context
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Uploads", NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(context: Context,text: String) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("File upload")
            .setContentText(text)
            .setSmallIcon(R.drawable.outline_cloud_upload_24)
            .setOngoing(true)
            .build()

    companion object {
        private const val CHANNEL_ID = "upload_channel"
        private const val NOTIFICATION_ID = 1
    }
}