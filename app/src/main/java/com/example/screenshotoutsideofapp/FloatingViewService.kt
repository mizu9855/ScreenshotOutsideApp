package com.example.screenshotoutsideofapp

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.util.*


class FloatingViewService : Service() {

    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"

        private var onClick: (() -> Unit)? = null
        fun setOnClickListener(onClick: () -> Unit) {
            this.onClick = onClick
        }
    }

    private val notificationId = Random().nextInt()

    private var floatingButton: FloatingButton? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_START) {
            startForeground(notificationId, createNotification())
            startOverlay()
        } else {
            stopForeground(true)
            stopOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopOverlay()
    }

    private fun createNotification(): Notification {
        val id = "floating_view_channel"
        val name = "Floating View"
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
            .setContentTitle(FloatingViewService::class.simpleName)
            .setContentText("スクリーンショット用ボタン表示中")
            .build()
    }

    private fun startOverlay() {
        floatingButton = FloatingButton(
            this,
            onClick
        ).apply {
            visible = true
        }
    }

    private fun stopOverlay() {
        floatingButton?.run {
            visible = false
            floatingButton = null
        }
    }
}