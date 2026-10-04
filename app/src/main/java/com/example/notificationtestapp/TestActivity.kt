package com.example.notificationtestapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notificationtestapp.databinding.ActivityMainBinding
import com.example.notificationtestapp.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private var counterService: CounterService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            counterService = (binder as CounterService.LocalBinder).getService()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            counterService = null // only called if the service crashes or is killed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, CounterService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        binding.incButton.setOnClickListener {
            onIncrementClick()
        }


    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        counterService = null
    }

    fun onIncrementClick() {
        val value = counterService?.increment()
        binding.counterTextView.text = value.toString()
    }
}