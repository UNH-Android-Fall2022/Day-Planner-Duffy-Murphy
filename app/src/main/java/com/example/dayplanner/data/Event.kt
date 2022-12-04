package com.example.dayplanner.data

import android.location.Location
import java.util.Date

data class Event(
    val startTime: Date? = null,
    val duration: Int = 900000,
    val eventName: String = "",
    val location: String = ""
)

val eventList: ArrayList<Event> = ArrayList()