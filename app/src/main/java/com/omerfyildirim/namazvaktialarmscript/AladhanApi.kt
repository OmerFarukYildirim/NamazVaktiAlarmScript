package com.omerfyildirim.namazvaktialarmscript

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 13 // Diyanet
    ): AladhanResponse
}

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings,
    val date: PrayerDate
)

data class PrayerTimings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

data class PrayerDate(
    val readable: String,
    val timestamp: String
)
