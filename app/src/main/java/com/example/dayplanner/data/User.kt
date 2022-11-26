package com.example.dayplanner.data

data class User(
    var defaultScreen: Int = 0,
    var locationServices: Boolean = false,
    var etaEnabled: Boolean = false,
    var startNotifications: Boolean = false,
    var endNotifications: Boolean = false
    )
