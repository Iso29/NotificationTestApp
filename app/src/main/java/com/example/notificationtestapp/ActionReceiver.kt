package com.example.notificationtestapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.notificationtestapp.Channels.ACTION_MARK_READ

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        when (intent.action) {
            ACTION_MARK_READ -> {
                val id = intent.getIntExtra("id", -1)

                if (id != -1) {
                    markAsRead(context, id)
                }
            }
        }
    }

    private fun markAsRead(
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