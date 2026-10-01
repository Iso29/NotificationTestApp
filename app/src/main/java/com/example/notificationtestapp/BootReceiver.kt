package com.example.notificationtestapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import kotlin.random.Random

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Hand off to WorkManager instead of doing work here


        showMessagingNotif(context, Random.nextInt())
    }

    fun showMessagingNotif(context: Context, id: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target", "inbox")
        }
        val tapPi = PendingIntent.getActivity(
            context, id, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val me = Person.Builder().setName("You").build()
        val style = NotificationCompat.MessagingStyle(me)
            .setConversationTitle("Android team")
            .addMessage(
                "Build is green ✅", System.currentTimeMillis(),
                Person.Builder().setName("Leyla").build()
            )

        val notification = NotificationCompat.Builder(context, Channels.NEWS)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)      // white-on-transparent vector
            .setContentTitle("New message")
            .setContentText("Your build finished successfully")
            .setSubText("This is sub text")
            .setColor(context.getColor(R.color.red))
            .setContentIntent(tapPi)
            .setOngoing(true)
            .setAutoCancel(true)
            .setStyle(style)// dismiss on tap
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // only matters < API 26
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            @SuppressLint("MissingPermission")
            nm.notify(id, notification)
        }
    }
}