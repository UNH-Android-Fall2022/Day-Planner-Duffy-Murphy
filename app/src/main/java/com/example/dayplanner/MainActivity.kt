package com.example.dayplanner

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.ktx.auth
import java.util.*


val TAG = "DayPlanner"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val db = Firebase.firestore

    companion object {
        var listAdapterPosition: Int = -1
    }
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
                R.id.navigation_list, R.id.navigation_planner, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val user = Firebase.auth.currentUser
        if (user != null)
            getEvents(user.uid)
    }

    private fun getEvents(uid: String) {

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
                    if (event.startTime == null || event.startTime?.after(currDate))
                        eventList.add(event)
//                    else //Delete it if it isn't today
//                        db.collection("Users/${user}/events").document(document.id).delete()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onStart() {
        super.onStart()


    }
}