package com.example.notificationtestapp

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PauseDownloadReceiver : BroadcastReceiver() {


    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val id = intent.getIntExtra("id",-1)
        val action = intent.getStringExtra("upload_action")?:""
        pauseDownload(context, id,action)

        //finish
    }

    private fun pauseDownload(
        context: Context,
        id: Int,
        action : String
    ) {
        // Handle mark as read
        // e.g. repository.markAsRead(id)
       customNotification(context,id,action)

        Log.d("PauseReceiver", "Mark notification $id as read")
    }

    fun customNotification(context : Context , id : Int,action : String) {
        val actionText = if(action == "pause") "resume" else "pause"
        val cancelPi = PendingIntent.getBroadcast(
            context, id,
            Intent(context, CancelReceiver::class.java)
                .putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pausePi = PendingIntent.getBroadcast(
            context, id,
            Intent(context, PauseDownloadReceiver::class.java)
                .putExtra("id", id)
                .putExtra("upload_action",actionText),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val smallBitmap = drawableToBitmap(context, R.drawable.image_profile)

        val collapsed = RemoteViews(context.packageName, R.layout.notif_collapsed).apply {
            setTextViewText(R.id.title, "Uploading report.pdf")
            setProgressBar(R.id.progress, 100, 40, false)
            setImageViewBitmap(R.id.cover, smallBitmap)          // keep bitmaps small
            setOnClickPendingIntent(R.id.btn_cancel, cancelPi)   // per-view clicks
        }

        val expanded = RemoteViews(context.packageName, R.layout.notif_expanded).apply {
            setImageViewBitmap(R.id.cover, smallBitmap)
            setTextViewText(R.id.title, "Uploading report.pdf")
            setTextViewText(R.id.subtitle, "12.4 MB of 30.0 MB")
            setProgressBar(R.id.progress, 100, 20, false)
            setTextViewText(R.id.percent, "$20%")
            setTextViewText(R.id.details, "1.2 MB/s · 15s left")
            setTextViewText(R.id.btn_pause, actionText)
            setOnClickPendingIntent(R.id.btn_pause, pausePi)
            setOnClickPendingIntent(R.id.btn_cancel, cancelPi)
        }

        val n = NotificationCompat.Builder(context, Channels.DOWNLOADS)
            .setSmallIcon(R.drawable.outline_cloud_upload_24)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())  // keeps system header/icon
            .setCustomContentView(collapsed)        // collapsed: keep it compact (~48–64dp)
            .setCustomBigContentView(expanded)      // expanded: max 256dp
            .setCustomHeadsUpContentView(collapsed) // optional
            .setOnlyAlertOnce(true)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            @SuppressLint("MissingPermission")
            nm.notify(id, n)
        }
    }
}