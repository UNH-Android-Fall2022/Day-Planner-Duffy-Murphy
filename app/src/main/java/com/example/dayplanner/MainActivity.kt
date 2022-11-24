package com.example.dayplanner

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.ktx.auth
import java.util.*


val TAG = "DayPlanner"
var DB_PULL_COMPLETED: Boolean = false
var CHANNEL_ID: String = "Day Planner App Notification Channel"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var channel: NotificationChannel
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val user = Firebase.auth.currentUser
        if (user != null)
            getEvents(user.uid)
    }

    override fun onStart() {
        super.onStart()


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
//Outside of class and not private so that it can be called if a user signs in after logging in as well
//May be changed back if I find a better method.
fun getEvents(uid: String) {
    val db = Firebase.firestore
    Log.d(TAG, "Getting already created events from Firestore")
    db.collection("Users/${uid}/events").get()
        .addOnSuccessListener { documents ->
            Log.d(TAG, "Document request succeeded")
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val currDate: Date = calendar.time

            for (document in documents) {
                val event: Event = document.toObject(Event::class.java)
                //Make sure event is today and not, say, a week ago
                if (event.startTime == null || event.startTime?.after(currDate)) {
                    //Needed for signin, because it uploads all current events before getting from db
                    if (!eventList.contains(event))
                        eventList.add(event)
                }
//                    else //Delete it if it isn't today
//                        db.collection("Users/${user}/events").document(document.id).delete()
                DB_PULL_COMPLETED = true
            }
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }
}