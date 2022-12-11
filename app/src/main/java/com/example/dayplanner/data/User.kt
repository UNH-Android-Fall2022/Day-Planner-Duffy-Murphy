package com.example.dayplanner.data

import java.util.Date

data class User(
    var defaultScreen: Int = 0,
    var etaEnabled: Boolean = false,
    var startNotifications: Boolean = false,
    var endNotifications: Boolean = false,
    var locationServices: Boolean = false,
    var eventsLastCleared: Date = Date()
    )
