package com.example.dayplanner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dayplanner.background.UserData
import com.example.dayplanner.data.User
import com.example.dayplanner.databinding.ActivityMainBinding
import com.example.dayplanner.background.UserData.Companion.startup
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.text.DateFormat
import java.util.*


val TAG = "DayPlanner"
val LOCAL_NOTIFICATION = "Local Notification"
var DB_PULL_COMPLETED: Boolean = false
var CHANNEL_ID: String = "Day Planner App Notification Channel"
var userData: User? = null

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var channel: NotificationChannel
    private lateinit var notificationManager: NotificationManager

    companion object {
        lateinit var context: Context
        var listAdapterPosition: Int = -1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        context = this
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list, R.id.navigation_planner, R.id.navigation_settings, R.id.navigation_login
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        createNotificationChannel()
        notificationManager.cancelAll()

        val timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
        Log.d(TAG, "Current time and day: ${timeFormat.format(Date())}")
        context.registerReceiver(UserData.Companion.NotificationAlarm, IntentFilter(LOCAL_NOTIFICATION))
        val user = Firebase.auth.currentUser
        if (user != null) {
            startup(user.uid)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}