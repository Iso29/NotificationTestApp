package com.example.notificationtestapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput

class ReplyReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        getTextFromIntent(context,intent)
    }

    private fun getTextFromIntent(
        context: Context,
        intent: Intent
    ) {
        val id = intent.getIntExtra("id", -1)
        val text = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(Channels.KEY_REPLY)
        Log.d("ReplyReceiver", "Mark notification $id as replied, ${text.toString()}")

        // Handle mark as read
        // e.g. repository.markAsRead(id)
        val nm = NotificationManagerCompat.from(context)
        nm.cancel(id)

    }
}