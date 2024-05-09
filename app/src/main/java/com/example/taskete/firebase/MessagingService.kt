package com.example.taskete.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.taskete.ui.activities.LoginFormActivity
import com.example.taskete.ui.activities.MainActivity
import com.example.taskete.R
import com.example.taskete.preferences.SessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


const val FIREBASE_TOKEN = "firebase_token"

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val body = remoteMessage.notification?.body
        sendNotification(body)
    }

    private fun sendNotification(body: String?) {
        val notificationBuilder =
            NotificationCompat.Builder(this, getString(R.string.push_notif_channel_id))
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(getText(R.string.push_notif_title))
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = if (SessionManager.restoreLoggedUser() != SessionManager.DEFAULT_USER_ID) {
            Intent(this, MainActivity::class.java)
        } else
            Intent(this, LoginFormActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            this, REQUEST_CODE, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.push_notif_channel_id),
                getText(R.string.push_notif_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(true)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val REQUEST_CODE = 0
        private const val NOTIFICATION_ID = 0

        fun printToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(FIREBASE_TOKEN, "Fetching FCM registration token failed")
                    return@OnCompleteListener
                }

                val token = task.result

                Log.d(FIREBASE_TOKEN, "$token")
            })

        }
    }
}