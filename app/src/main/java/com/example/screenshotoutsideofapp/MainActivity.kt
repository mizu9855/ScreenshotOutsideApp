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

        val activityIntent = Intent(this, CaptureActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(activityIntent)
        startFloatingViewService()
    }

    override fun onDestroy() {
        super.onDestroy()
        val floatingServiceIntent = Intent(this, FloatingViewService::class.java)
            .setAction(FloatingViewService.ACTION_STOP)
        startService(floatingServiceIntent)

        val captureServiceIntent = Intent(this, CaptureService::class.java)
            .setAction(CaptureService.ACTION_STOP)
        startService(captureServiceIntent)
    }

    private fun startFloatingViewService() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            launcher.launch(intent)
        }
    }
}