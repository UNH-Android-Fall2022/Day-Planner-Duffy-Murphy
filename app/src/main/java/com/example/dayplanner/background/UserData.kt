package com.example.dayplanner.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dayplanner.*
import com.example.dayplanner.MainActivity.Companion.context
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.User
import com.example.dayplanner.data.eventList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextInt

class UserData {

    //Most of the inspiration for this comes from
    //https://proandroiddev.com/everything-you-need-to-know-about-adding-notifications-with-alarm-manager-in-android-cb94a92b3235
    private class NotificationAlarm: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val eventName: String? = p1?.extras?.getString("name")
            val _eventStart: Boolean? = p1?.extras?.getBoolean("eventStart")
            val _id: Int? = p1?.extras?.getInt("id")
            val eventStart: Boolean = if (_eventStart != null) _eventStart else true
            val id: Int = if (_id != null) _id else nextInt(0, Int.MAX_VALUE - 1)

            val tapIntent: Intent =  Intent (p0, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(p0, 0, tapIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            var builder: NotificationCompat.Builder?

            if (eventStart) {
                builder = p0?.let {
                    NotificationCompat.Builder(it, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setContentTitle("Event Started")
                        .setContentText("Time for ${eventName}")
                }
            } else {
                builder = p0?.let {
                    NotificationCompat.Builder(it, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setContentTitle("Event Ended")
                        .setContentText("Time to stop ${eventName}")
                }
            }

            builder?.let {
                p0?.let { with(NotificationManagerCompat.from(it)) {
                    notify(id, builder.build())
                } }
            }


        }
    }

    companion object {
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        private val alarmList: ArrayList<Intent> = ArrayList()

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
                    val currDate: Date = calendar.time

                    for (document in documents) {
                        val event: Event = document.toObject(Event::class.java)
                        //Make sure event is today and not, say, a week ago
                        if (event.startTime == null || event.startTime.after(currDate)) {
                            //Needed for signin, because it uploads all current events before getting from db
                            if (!eventList.contains(event))
                                eventList.add(event)
                        }
//                    else //Delete it if it isn't today
//                        db.collection("Users/${user}/events").document(document.id).delete()
                        DB_PULL_COMPLETED = true
                    }
                    //Needs to be called in order for certain functions
                    getUser(uid)
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
                    userData = user.toObject(User::class.java)
                    //Needs to be called in order for this function to work
                    setAllAlarms()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user: ", exception)
                }
        }

        //Must include old event to update event! Otherwise, this function has no way of knowing for sure it's the same event
        fun setAlarm(event: Event, oldEvent: Event? = null) {
            if (event.startTime != null && userData != null) {

                if (oldEvent == null) {
                    if (userData!!.startNotifications)
                    {
                        val eventTime = event.startTime.time
                        val id = nextInt(0, Int.MAX_VALUE - 1)

                        val alarmIntent = Intent(context, NotificationAlarm::class.java)
                        alarmIntent.putExtra("id", id)
                        alarmIntent.putExtra("eventName", event.eventName)
                        alarmIntent.putExtra("eventStart", true)
                        alarmIntent.putExtra("startTime", eventTime)

                        val pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventTime, pendingIntent)
                        alarmList.add(alarmIntent)
                    }
                    if (userData!!.endNotifications)
                    {
                        val eventTime = event.startTime.time + event.duration
                        val id = nextInt(0, Int.MAX_VALUE - 1)

                        val alarmIntent = Intent(context, NotificationAlarm::class.java)
                        alarmIntent.putExtra("id", id)
                        alarmIntent.putExtra("eventName", event.eventName)
                        alarmIntent.putExtra("eventStart", false)
                        alarmIntent.putExtra("startTime", eventTime)

                        val pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, eventTime, pendingIntent)
                        alarmList.add(alarmIntent)
                    }
                } else {
                    for (intent in alarmList)
                    {
                        val eventStart = oldEvent.startTime!!.time
                        val eventEnd = eventStart + oldEvent.duration

                        val alarmTime = intent.getLongExtra("startTime", 0)
                        if (alarmTime == eventStart || alarmTime == eventEnd)
                        {
                            val id = intent.getIntExtra("id", 0)
                            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                            alarmManager.cancel(pendingIntent)
                            alarmList.remove(intent)
                        }
                    }
                    setAlarm(event)
                }

            } else if (event.startTime == null) {
                Log.w(TAG, "setAlarm was called for an event without a start time")
            } else {
                Log.w(TAG, "setAlarm was called without a user")
            }
        }

        fun clearAlarm(event: Event) {
            clearOneAlarm(event, true)
            clearOneAlarm(event, false)
        }

        private fun clearOneAlarm(event: Event, startAlarm: Boolean)
        {
            if (event.startTime != null) {
                val alarmTime = if (startAlarm) event.startTime.time else event.startTime.time + event.duration
                for (intent in alarmList) {
                    if (alarmTime == intent.getLongExtra("startTime", 0)) {
                        val id = intent.getIntExtra("id", 0)
                        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.cancel(pendingIntent)
                        alarmList.remove(intent)
                    }
                }
            } else {
                Log.w(TAG, "Tried to delete alarm for an event without a start time")
            }
        }

        private fun setAllAlarms() {
            for (event in eventList)
            {
                if (event.startTime != null)
                    setAlarm(event)
            }
        }

        private fun clearAllAlarms() {
            for (event in eventList)
            {
                if (event.startTime != null) {
                    clearAlarm(event)
                }
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
                Log.d(TAG, "Getting events from database")
                getEvents(user.uid)
                DB_PULL_COMPLETED = true
            }
        }

        fun logout() {
            clearAllAlarms()
            eventList.clear()
            DB_PULL_COMPLETED = false
            userData = null
        }

        fun startup (uid: String) {
            getEvents(uid)
        }
    }
}

