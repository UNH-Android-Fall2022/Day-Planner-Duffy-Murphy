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
import com.firebase.ui.auth.AuthUI
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.ktx.auth
import java.util.*


val TAG = "DayPlanner"
var dbPullCompleted: Boolean = false

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        val user = Firebase.auth.currentUser
        if (user != null)
            getEvents(user.uid)
    }

    override fun onStart() {
        super.onStart()


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
                dbPullCompleted = true
            }
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }
}