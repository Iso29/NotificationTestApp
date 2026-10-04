package com.example.notificationtestapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class CounterService : Service() {

    private val binder = LocalBinder()
    private var count = 0

    inner class LocalBinder : Binder() {
        fun getService(): CounterService = this@CounterService
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun increment(): Int = ++count


}