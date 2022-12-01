package com.example.screenshotoutsideofapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        startFloatingViewService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startFloatingViewService()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFloatingViewService()
    }

    private fun startFloatingViewService() {
        if (Settings.canDrawOverlays(this)) {
            val serviceIntent = Intent(this, FloatingViewService::class.java)
                .setAction(FloatingViewService.ACTION_START)
            startForegroundService(serviceIntent)
            FloatingViewService.setOnClickListener {
                // TODO
            }
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            launcher.launch(intent)
        }
    }

    private fun stopFloatingViewService() {
        val serviceIntent = Intent(this, FloatingViewService::class.java)
            .setAction(FloatingViewService.ACTION_STOP)
        startForegroundService(serviceIntent)
    }
}