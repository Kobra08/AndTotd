package com.example.taskete

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy


class TasksApplication : Application() {
    override fun onCreate() {
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork() // or .detectAll() for all detectable problems
                        .penaltyLog()
                        .build()
        )
        StrictMode.setVmPolicy(
                VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .penaltyDeath()
                        .build()
        )
        super.onCreate()
    }
}