package com.example.dayplanner.data

import java.util.Date

data class Event(
    val startTime: Date?,
    val duration: Int,
    val eventName: String
)

val eventList: ArrayList<Event> = ArrayList()