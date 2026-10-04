package com.example.notificationtestapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notificationtestapp.databinding.ActivityCustomForegroundServiceBinding

class CustomForegroundServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomForegroundServiceBinding

    // Coming back from the Settings screen. resultCode is always CANCELED here,
    // so check the permission itself.
    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                startOverlay()
            } else {
                Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    // The service runs either way. This only makes its notification visible.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkOverlayAndStart()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomForegroundServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartOverlay.setOnClickListener { onStartClicked() }
        binding.btnStopOverlay.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
        }
    }

    private fun onStartClicked() {
        val needsNotificationPermission = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            checkOverlayAndStart()
        }
    }

    private fun checkOverlayAndStart() {
        if (Settings.canDrawOverlays(this)) {
            startOverlay()
        } else {
            overlayPermissionLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
            )
        }
    }

    private fun startOverlay() {
        ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
        moveTaskToBack(true) // optional: go to the home screen so the user can open a game
    }
}