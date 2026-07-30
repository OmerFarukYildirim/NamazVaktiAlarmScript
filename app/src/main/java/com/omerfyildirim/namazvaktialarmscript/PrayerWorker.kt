package com.omerfyildirim.namazvaktialarmscript

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class PrayerWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("PrayerWorker", "Worker started")
        return try {
            val location = getCurrentLocation()
            if (location != null) {
                Log.d("PrayerWorker", "Location obtained: ${location.latitude}, ${location.longitude}")
                val timings = fetchPrayerTimings(location.latitude, location.longitude)
                setAlarms(applicationContext, timings)
                Result.success()
            } else {
                Log.e("PrayerWorker", "Could not get location")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("PrayerWorker", "Error in Worker", e)
            Result.failure()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        return try {
            val task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            Tasks.await(task)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchPrayerTimings(lat: Double, lon: Double): PrayerTimings {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(AladhanApi::class.java)
        val response = api.getTimings(lat, lon)
        return response.data.timings
    }

    private fun setAlarms(context: Context, timings: PrayerTimings) {
        val prayerTimes = listOf(
            "Sabah" to timings.Fajr,
            "Öğle" to timings.Dhuhr,
            "İkindi" to timings.Asr,
            "Akşam" to timings.Maghrib,
            "Yatsı" to timings.Isha
        )

        Log.d("PrayerWorker", "Starting to set alarms using AlarmClock.ACTION_SET_ALARM")

        for ((name, time) in prayerTimes) {
            val (hour, minute) = time.split(":").map { it.toInt() }
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.add(Calendar.MINUTE, -5) // 5 minutes before

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                Log.d("PrayerWorker", "Skipping $name ($time) because it's in the past")
                continue
            }

            val alarmHour = calendar.get(Calendar.HOUR_OF_DAY)
            val alarmMinute = calendar.get(Calendar.MINUTE)

            Log.d("PrayerWorker", "Setting system alarm for $name at $alarmHour:$alarmMinute")
            
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, "$name Namazı")
                putExtra(android.provider.AlarmClock.EXTRA_HOUR, alarmHour)
                putExtra(android.provider.AlarmClock.EXTRA_MINUTES, alarmMinute)
                putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            try {
                context.startActivity(intent)
                // Small delay to prevent system from being overwhelmed by multiple intents
                Thread.sleep(500) 
            } catch (e: Exception) {
                Log.e("PrayerWorker", "Failed to start AlarmClock intent for $name", e)
            }
        }
    }
}
