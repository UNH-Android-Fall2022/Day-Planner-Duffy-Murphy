package com.example.dayplanner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dayplanner.background.Alarms
import com.example.dayplanner.background.Session.Companion.startup
import com.example.dayplanner.data.User
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
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
        var location: String? = null
        var appWasJustStarted = true
        var cameFromMapsActivity = false
        val numEventListProperties = 5
        val evtListFileName = "EventList.txt"
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
                R.id.navigation_list,
                R.id.navigation_planner,
                R.id.navigation_settings,
                R.id.navigation_login,
                R.id.navigation_list_add,
                R.id.navigation_splash_screen
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        createNotificationChannel()


        val timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
        Log.d(TAG, "Current time and day: ${timeFormat.format(Date())}")
        context.registerReceiver(Alarms.NotificationAlarm, IntentFilter(LOCAL_NOTIFICATION))
        val user = Firebase.auth.currentUser
        if (user != null) {
            startup(user.uid)
        }
    }

    // Strictly for returning from Google map
    override fun onResume() {
        super.onResume()

        notificationManager.cancelAll()

        location = this.intent.extras?.getString("loc")
        // Crude way to check if we are returning from the MapsActivity
        if (location != null) {
            cameFromMapsActivity = true
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_list_add)
        }
        this.intent.removeExtra("loc")
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

    override fun onStop() {
        super.onStop()
        // NEW: Save data to internal storage if user is not signed in
        if (FirebaseAuth.getInstance().currentUser == null) {
            // Check if there is enough storage space
            // Since this is time consuming we will just overestimate - 1 KB per each event
            // Adapted from https://stackoverflow.com/questions/8133417/android-get-free-size-of-internal-external-memory
            val path = context.filesDir
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val kilobytesAvailable = availableBlocks * blockSize / 1024
            if (eventList.size < kilobytesAvailable.toInt()) {
                // The following is adapted from https://stackoverflow.com/questions/35444264/how-do-i-write-to-a-file-in-kotlin
                File(context.filesDir, evtListFileName).printWriter().use { out ->
                    eventList.forEach {
                        out.println(it.startTime.toString()) // startTime is Date()?
                        out.println(it.duration.toString()) // duration is Int
                        out.println(it.eventName) // String
                        out.println(it.location) // String
                        out.println(it.recurring.toString()) // Int
                    }
                }
            } else {
                // Not enough kilobytes available
                // No need to make a toast since the user is closing the app anyways
            }
        }
    }
}
