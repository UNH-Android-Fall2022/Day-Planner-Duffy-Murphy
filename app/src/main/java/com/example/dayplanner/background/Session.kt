package com.example.dayplanner.background

import android.util.Log
import com.example.dayplanner.DB_PULL_COMPLETED
import com.example.dayplanner.MainActivity
import com.example.dayplanner.TAG
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.User
import com.example.dayplanner.data.eventList
import com.example.dayplanner.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import com.example.dayplanner.background.Alarms
import com.example.dayplanner.background.Alarms.Companion.clearAllAlarms
import com.example.dayplanner.background.Alarms.Companion.setAllAlarms
import com.google.firebase.firestore.SetOptions

class Session {
    companion object {
        private fun getEvents(uid: String) {
            val db = Firebase.firestore

            Log.d(TAG, "Getting already created events from Firestore")
            db.collection("Users/${uid}/events").get()
                .addOnSuccessListener { documents ->
                    Log.d(TAG, "Document request succeeded")

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val currDay: Date = calendar.time

                    val eventsLastCleared = userData?.eventsLastCleared

                    if (eventsLastCleared != null && currDay.before(eventsLastCleared)) {
                        for (document in documents) {
                            val event: Event = document.toObject(Event::class.java)
                            if (event.recurring == 0 || //TODO: Add more types of recurring events
                                event.recurring == 1) {
                                //Needed for signin, because it uploads all current events before getting from db
                                if (!eventList.contains(event))
                                    eventList.add(event)
                            }
                        }

                        Log.d(TAG, "Setting alarms")
                        //Needs to be called in order so this function can work
                        setAllAlarms()
                    } else {
                        // We need to clear it in the app because Firestore's TTL feature
                        // Can take up to 72 hours
                        Log.d(TAG, "Events last cleared before today began. Clearing all events")
                        for (document in documents) {
                            val event: Event = document.toObject(Event::class.java)
                            if (event.recurring == 0) {
                                db.collection("Users/${uid}/events")
                                    .document(document.id)
                                    .delete()
                            }
                        }
                        val user = FirebaseAuth.getInstance().currentUser
                        userData?.eventsLastCleared = Date()
                        userData?.let {
                            Firebase.firestore.collection("Users")
                                .document(user!!.uid)
                                .set(it, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(TAG, "Successfully updated clear date")
                                }
                        }
                    }
                    DB_PULL_COMPLETED = true // End of on success listener
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }

        private fun getUser(uid: String) {
            val db = Firebase.firestore
            Log.d(TAG, "Getting user settings from Firestore")
            db.collection("Users").document(uid).get()
                .addOnSuccessListener { user ->
                    Log.d(TAG, "User data pulled")
                    if (user.exists()) {
                        userData = user.toObject(User::class.java)
                        Log.d(TAG, "User data: $userData")
                    } else {
                        userData = User()
                    }

                    Log.d(TAG, "Getting events from database")
                    //Needs to be called in order so certain functions work
                    getEvents(uid) // DB_PULL_COMPLETED set in here
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user: ", exception)
                }
        }

        fun login() {
            val db = Firebase.firestore
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                Log.d(TAG, "Uploading current events to database")
                for (event in eventList) {
                    db.collection("Users/${user.uid}/events").add(event)
                        .addOnSuccessListener { Log.d(TAG, "Event successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document: ", e) }
                }
                Log.d(TAG, "Getting user from database")
                getUser(user.uid)
//                DB_PULL_COMPLETED = true
            }
        }

        fun logout() {
            clearAllAlarms()
            eventList.clear()
            DB_PULL_COMPLETED = false
            userData = null
            MainActivity.location = null
        }

        fun startup (uid: String) {
            getUser(uid)
        }
    }
}