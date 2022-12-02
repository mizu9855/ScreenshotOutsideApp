package com.example.screenshotoutsideofapp

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.util.*


class CaptureService : Service() {

    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
    }

    private val notificationId = Random().nextInt()

    override fun onCreate() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_START) {
            startForeground(notificationId, createNotification())
            CaptureActivity.setMediaProjection()
        } else {
            stopForeground(true)
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        disableCapture()
    }

    private fun createNotification(): Notification {
        val id = "capture_service_channel"
        val name = "Capture Service"
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return Notification.Builder(this, id)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(CaptureService::class.simpleName)
            .setContentText("スクリーンショットサービス起動中")
            .build()
    }

    private fun disableCapture() {
        CaptureActivity.mediaProjection = null
    }
}