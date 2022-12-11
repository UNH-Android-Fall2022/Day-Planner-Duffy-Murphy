package com.example.dayplanner.data

import android.location.Location
import java.util.Date

//TODO: Add something like an enum for recurring events

data class Event(
    val startTime: Date? = null,
    val duration: Int = 900000,
    val eventName: String = "",
    val location: String = "",
    val recurring: Int = 0 //0 = not recurring, 1 = daily, 2 = weekly, 3 = monthly, 4 = yearly
)

val eventList: ArrayList<Event> = ArrayList()