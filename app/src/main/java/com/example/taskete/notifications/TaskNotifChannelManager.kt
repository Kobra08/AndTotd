package com.example.taskete.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskete.R
import com.example.taskete.data.Task
import com.example.taskete.preferences.SessionManager

object TaskNotifChannelManager {

    fun createNotificationReminderChannel(context: Context): String {
        val TASK_REMINDER_CHANNELID = context.resources.getText(R.string.channelId).toString()
        val TASK_REMINDER_NAME = context.resources.getText(R.string.channelName)
        val TASK_REMINDER_DESCRIPTION = context.resources.getText(R.string.channelDesc).toString()

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    TASK_REMINDER_CHANNELID,
                    TASK_REMINDER_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
            )

            channel.description = TASK_REMINDER_DESCRIPTION
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }

        return TASK_REMINDER_CHANNELID
    }
}


