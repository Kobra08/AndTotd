package com.example.taskete.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.example.taskete.ui.activities.LOGGED_USER
import com.example.taskete.R
import com.example.taskete.ui.activities.TASK_SELECTED
import com.example.taskete.ui.activities.TaskFormActivity
import com.example.taskete.data.Task
import com.example.taskete.data.User
import com.example.taskete.db.TasksDAO
import com.example.taskete.db.UsersDAO
import com.example.taskete.extensions.stringFromDate
import com.example.taskete.preferences.SessionManager
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

//DEBUGGING
private const val TAG_ACTIVITY = "NotificationService"
const val JOB_ID = 100
const val DEFAULT_ID = 1000

class NotificationService : JobIntentService() {

    private val channelId: String by lazy {
        TaskNotifChannelManager.createNotificationReminderChannel(this)
    }

    private val usersDAO by lazy {
        UsersDAO(this@NotificationService.applicationContext)
    }

    private val tasksDAO by lazy {
        TasksDAO(this@NotificationService.applicationContext)
    }

    private var task: Task? = null
    private var user: User? = null
    private var flagUserhOK = false
    private var flagTaskOK = false
    private var compositeDisposable = CompositeDisposable()

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            //Older versions (before API 26 (Oreo)) will use startService
            enqueueWork(context, NotificationService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val taskId = intent.extras?.getInt(TASK_SELECTED)
        val userId = intent.extras?.getInt(LOGGED_USER)

        if (userId == SessionManager.restoreLoggedUser()) {
            getTask(taskId)
            getUser(userId)

            //Set notification
            Handler(mainLooper).postDelayed({
                setNotification()
            }, 3000)

        } else
            Log.d(TAG_ACTIVITY, "Cant show notification because task user != current logged user")

    }

    private fun getTask(taskId: Int?) {
        taskId?.let {
            tasksDAO.getTask(taskId).subscribe(object : SingleObserver<Task?> {
                override fun onSubscribe(d: Disposable?) {
                    compositeDisposable.add(d)
                }

                override fun onSuccess(t: Task?) {
                    task = t
                    flagTaskOK = true
                }

                override fun onError(e: Throwable?) {
                    Log.d(TAG_ACTIVITY, "Error retrieving task because ${e?.message}")
                }

            })
        }
    }


    private fun getUser(userId: Int?) {
        userId?.let {
            usersDAO.getUser(userId).subscribe(object : SingleObserver<User?> {
                override fun onSubscribe(d: Disposable?) {
                    compositeDisposable.add(d)
                }

                override fun onSuccess(t: User?) {
                    user = t
                    flagUserhOK = true
                }

                override fun onError(e: Throwable?) {
                    Log.d(TAG_ACTIVITY, "Error retrieving user because ${e?.message}")
                }

            })
        }
    }

    private fun setNotification() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(task)
        notification.contentIntent = setNotifIntent(task)

        notificationManager.notify(task?.id ?: DEFAULT_ID, notification)

        sendConfirmationLog()
    }

    //Notification intent to show task data in the TaskFormActivity
    private fun setNotifIntent(task: Task?): PendingIntent {
        val notifIntent = Intent(this, TaskFormActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
            putExtra(TASK_SELECTED, task)
            putExtra(LOGGED_USER, user)
        }

        return PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createNotification(task: Task?): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getText(R.string.reminderTitle))
            .setContentText("${user?.username} you need to check '${task?.title}'!")
            .setSmallIcon(R.drawable.ic_app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(channelId)
            .setAutoCancel(true)
            .build()
    }

    //Confirmation log
    private fun sendConfirmationLog() {
        val calendar = GregorianCalendar()
        calendar.timeInMillis = System.currentTimeMillis()
        val date = calendar.time.stringFromDate()
        Log.d(TAG_ACTIVITY, "Notification was successfully sent at $date")
    }

    override fun onDestroy() {
        if (flagTaskOK && flagUserhOK) {
            compositeDisposable.clear()
        }
        super.onDestroy()
    }
}

