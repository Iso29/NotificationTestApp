package com.example.notificationtestapp

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

object Channels {
    const val GENERAL = "general"
    const val PLAYBACK = "playback"
    const val DOWNLOADS = "downloads"

    const val NEWS = "news"

    const val ACTION_MARK_READ = "com.example.notificationtestapp.ACTION_MARK_READ"

    const val KEY_REPLY = "reply"

    fun create(context: Context) {
        val channels = listOf(
            NotificationChannelCompat.Builder(NEWS, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("NEWS").setDescription("About app news").build(),
            NotificationChannelCompat.Builder(GENERAL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("General").setDescription("Account and app updates").build(),
            NotificationChannelCompat.Builder(PLAYBACK, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Playback").setShowBadge(false).build(),
            NotificationChannelCompat.Builder(DOWNLOADS, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Downloads").setShowBadge(false).build(),
        )
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(channels)
    }
}