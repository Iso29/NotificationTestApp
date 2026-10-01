package com.example.notificationtestapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class CancelReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val id = intent.getIntExtra("id",-1)
        cancelDownload(context, id)
    }

    private fun cancelDownload(
        context: Context,
        id: Int
    ) {
        // Handle mark as read
        // e.g. repository.markAsRead(id)
        val nm = NotificationManagerCompat.from(context)
        nm.cancel(id)

        Log.d("ActionReceiver", "Mark notification $id as read")
    }
}