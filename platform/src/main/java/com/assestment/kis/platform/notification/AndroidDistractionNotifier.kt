package com.assestment.kis.platform.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.platform.R

class AndroidDistractionNotifier(private val context: Context) : DistractionNotifier {

    init {
        createChannel()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun notifyDistraction(event: DistractionEvent) {
        if (!canPostNotifications()) return

        val reasonRes = when (event.type) {
            DistractionType.NOISE -> R.string.notification_noise
            DistractionType.MOVEMENT -> R.string.notification_movement
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(reasonRes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter(NOTIFICATION_TIMEOUT_MILLIS)
            .build()

        // One id per type, so a fresh alert of the same kind replaces the previous one.
        NotificationManagerCompat.from(context).notify(event.type.ordinal, notification)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "focus_distractions"
        const val NOTIFICATION_TIMEOUT_MILLIS = 6_000L
    }
}
