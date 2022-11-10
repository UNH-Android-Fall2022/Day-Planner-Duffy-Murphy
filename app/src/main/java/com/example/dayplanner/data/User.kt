package com.example.dayplanner.data

data class User(
    val defaultScreen: Int = 0,
    val locationServices: Boolean = false,
    val etaEnabled: Boolean = false,
    val startNotifications: Boolean = false,
    val endNotifications: Boolean = false
    )
