package com.example.notificationtestapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.RemoteViews
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.notificationtestapp.Channels.ACTION_MARK_READ
import com.example.notificationtestapp.Channels.KEY_REPLY
import com.example.notificationtestapp.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val launcher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.postValue(granted)
        if(!granted) {
            intentToSettings()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.batteryPercent()
            binding.batteryText.text = level?.let { "Battery: $it%" } ?: "Battery: —"
        }
    }

    private val hasPermission = MutableLiveData(false)

    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initials()
        setUpObserver()
        setUpListeners()
    }

    private fun showMediaPlayerStyle(id : Int) {
        val avatar = drawableToBitmap(
            this,
            R.drawable.image_profile
        )
        val notification = NotificationCompat.Builder(
            this,
            Channels.NEWS
        )
            .setSmallIcon(R.drawable.outline_cloud_upload_24)
            .setContentTitle("My Song")
            .setContentText("My Artist")
            .setLargeIcon(avatar)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
//            .setStyle(
//                MediaStyleNotificationHelper.MediaStyle(MediaSession())
//            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateSwitcher(isChecked: Boolean) {
        with(binding) {
            permissionSwitcher.apply {
                this.isChecked = isChecked
                if (isChecked) {
                    thumbTintList = getColorStateList(R.color.green)
                    trackTintList = getColorStateList(R.color.dark_green)
                } else {
                    thumbTintList = getColorStateList(R.color.red)
                    trackTintList = getColorStateList(R.color.dark_red)
                }
            }
        }
    }

    private fun initials() {
        Channels.create(this)
        val isGranted = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat
                .checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        hasPermission.postValue(isGranted)
    }

    private fun setUpObserver() {
        hasPermission.observe(this, ::updateSwitcher)
    }

    private fun setUpListeners() {
        with(binding) {
            permissionSwitcher.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    requestForPermission()
                } else {
//                    intentToSettings()
                }
            }

            showBasicNotification.setOnClickListener {
                val notifId = Random.nextInt(10000)
                showBasic(this@MainActivity, notifId)
            }

            showMessagingNotif.setOnClickListener {
                val notifId = Random.nextInt(10000)
                showMessagingNotif(this@MainActivity, notifId)
            }

            customMessageBtn.setOnClickListener {
                val notifId = Random.nextInt(10000)
                customNotification(this@MainActivity,notifId)
            }

            startSyncService.setOnClickListener {
                ContextCompat.startForegroundService(this@MainActivity,
                    Intent(this@MainActivity, UploadService::class.java)
                )
            }
        }
    }

    fun showBasic(context: Context, id: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target", "inbox")
        }

        val tapPi = PendingIntent.getActivity(
            context, id, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val avatar = drawableToBitmap(
            this,
            R.drawable.image_profile
        )

        val markReadPi = PendingIntent.getBroadcast(
            context, id,
            Intent(context, ActionReceiver::class.java)
                .setAction(ACTION_MARK_READ)
                .putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteInput = androidx.core.app.RemoteInput.Builder(KEY_REPLY).setLabel("Reply").build()
        val replyPi = PendingIntent.getBroadcast(
            context, Random.nextInt(),
            Intent(
                context,
                ReplyReceiver::class.java
            ).apply {
                putExtra("id", id)
            },            // explicit intent (required on 14+)
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.outline_arrows_more_up_24, "Reply", replyPi
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()


        val notification = NotificationCompat.Builder(context, Channels.GENERAL)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)      // white-on-transparent vector
            .setContentTitle("New message")
            .setContentText("Your build finished successfully")
            .setSubText("This is sub text")
            .setColor(getColor(R.color.red))
            .setLargeIcon(avatar)
            .setContentIntent(tapPi)
            .setAutoCancel(true)
            .addAction(replyAction)
            .addAction(R.drawable.outline_check_24, "Mark as read", markReadPi)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            @SuppressLint("MissingPermission")
            nm.notify(id, notification)
        }
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
            .addMessage("Build is green ✅", System.currentTimeMillis(),
                Person.Builder().setName("Leyla").build())

        val notification = NotificationCompat.Builder(context, Channels.NEWS)
            .setSmallIcon(R.drawable.outline_circle_notifications_24)      // white-on-transparent vector
            .setContentTitle("New message")
            .setContentText("Your build finished successfully")
            .setSubText("This is sub text")
            .setColor(getColor(R.color.red))
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

    fun customNotification(context : Context , id : Int) {
        val cancelPi = PendingIntent.getBroadcast(
            context, id,
            Intent(context, CancelReceiver::class.java)
                .putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pausePi = PendingIntent.getBroadcast(
            context, id,
            Intent(context, PauseDownloadReceiver::class.java)
                .putExtra("upload_action" ,"pause")
                .putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val smallBitmap = drawableToBitmap(this, R.drawable.image_profile)

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
            setTextViewText(R.id.btn_pause, "Pause")
            setOnClickPendingIntent(R.id.btn_pause, pausePi)
            setOnClickPendingIntent(R.id.btn_cancel, cancelPi)
        }

        val n = NotificationCompat.Builder(context, Channels.NEWS)
            .setSmallIcon(R.drawable.outline_cloud_upload_24)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setCustomHeadsUpContentView(collapsed)
            .setOnlyAlertOnce(true)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (nm.areNotificationsEnabled()) {
            @SuppressLint("MissingPermission")
            nm.notify(id, n)
        }
    }

    private fun requestForPermission() {
        if (Build.VERSION.SDK_INT >= 33 && hasPermission.value == false) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun intentToSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${this.packageName}".toUri()
        )
        startActivity(intent)
    }
}