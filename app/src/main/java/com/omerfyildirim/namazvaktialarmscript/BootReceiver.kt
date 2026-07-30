package com.omerfyildirim.namazvaktialarmscript

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("BootReceiver", "Received intent: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            schedulePrayerWork(context)
        }
    }

    private fun schedulePrayerWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Run immediately after boot to restore alarms
        val immediateWork = OneTimeWorkRequestBuilder<PrayerWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "PrayerWorkImmediate",
            ExistingWorkPolicy.REPLACE,
            immediateWork
        )

        // 2. Ensure periodic schedule is maintained
        val workRequest = PeriodicWorkRequestBuilder<PrayerWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PrayerWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
