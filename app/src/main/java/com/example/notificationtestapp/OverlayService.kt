package com.example.notificationtestapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.view.isVisible
import com.example.notificationtestapp.databinding.OverlayBubbleBinding
import kotlin.math.hypot

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var binding: OverlayBubbleBinding

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // lets us collapse on outside tap
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 400
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        ServiceCompat.startForeground(
            this, 1, buildNotification(),
            if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        )

        // A Service has no theme. Without this, ?attr/ references and Material widgets crash on inflate.
        val themed = ContextThemeWrapper(this, R.style.Theme_NotificationTestApp)
        binding = OverlayBubbleBinding.inflate(LayoutInflater.from(themed))

        setupDrag()
        setupOutsideTouch()
        binding.bubble.setOnClickListener { setPanelOpen(!binding.panel.isVisible) }
        binding.btnDnd.setOnClickListener { /* your action */ }
        binding.btnClose.setOnClickListener { stopSelf() }

        wm.addView(binding.root, params)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrag() {
        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var startX = 0
        var startY = 0
        var downX = 0f
        var downY = 0f
        var dragging = false

        binding.bubble.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    downX = e.rawX
                    downY = e.rawY
                    dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    // rawX/rawY are screen coordinates, so moving the window doesn't distort them
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (!dragging && hypot(dx, dy) > slop) dragging = true
                    if (dragging) {
                        params.x = startX + dx.toInt()
                        params.y = startY + dy.toInt()
                        wm.updateViewLayout(binding.root, params)
                    }
                }
                MotionEvent.ACTION_UP -> if (!dragging) v.performClick()
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideTouch() {
        binding.root.setOnTouchListener { _, e ->
            if (e.actionMasked == MotionEvent.ACTION_OUTSIDE && binding.panel.isVisible) {
                setPanelOpen(false)
            }
            false
        }
    }

    private fun setPanelOpen(open: Boolean) {
        binding.panel.isVisible = open
        // WRAP_CONTENT resizes the window automatically; no params change needed
    }

    private fun buildNotification(): Notification {
        val channelId = "game_overlay"
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Game overlay")
                .build()
        )
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(androidx.media3.session.R.drawable.media3_icon_block)
            .setContentTitle("Game toolbar is running")
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        if (::binding.isInitialized && binding.root.isAttachedToWindow) {
            wm.removeView(binding.root)
        }
        super.onDestroy()
    }
}