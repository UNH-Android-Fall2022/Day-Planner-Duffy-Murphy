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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextInt

class UserData {
    companion object {
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        private val alarmList: ArrayList<Intent> = ArrayList()

        //Most of the inspiration for this comes from
        //https://proandroiddev.com/everything-you-need-to-know-about-adding-notifications-with-alarm-manager-in-android-cb94a92b3235
        val NotificationAlarm = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(TAG, "NotificationAlarm onReceive function was called")
                val eventName: String? = p1?.extras?.getString("eventName")
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
                        Log.d(TAG, "notification sent")
                    } }
                }
            }
        }

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
                    //Needs to be called in order so certain functions work
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
                    if (user.exists()) {
                        userData = user.toObject(User::class.java)
                        Log.d(TAG, "User data: ${userData}")
                    } else {
                        userData = User()
                    }
                    //Needs to be called in order so this function can work
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
                    if (userData!!.startNotifications) {
                        val eventTime = event.startTime.time
                        if (Date(eventTime).after(Date())) {
                            val id = nextInt(0, Int.MAX_VALUE - 1)

                            val alarmIntent = Intent(LOCAL_NOTIFICATION)
                                .putExtra("id", id)
                                .putExtra("eventName", event.eventName)
                                .putExtra("eventStart", true)
                                .putExtra("startTime", eventTime)

                            val pendingIntent = PendingIntent.getBroadcast(
                                context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP, eventTime, pendingIntent)
                            alarmList.add(alarmIntent)

                            val timeFormat =
                                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
                            Log.d(
                                TAG,
                                "Alarm with id ${id.toString()} and startTime ${
                                    timeFormat.format(Date(eventTime))
                                } created for ${event.eventName}"
                            )
                        }
                    }
                    if (userData!!.endNotifications) {
                        val eventTime = event.startTime.time + event.duration
                        if (Date(eventTime).after(Date())) {
                            val id = nextInt(0, Int.MAX_VALUE - 1)

                            val alarmIntent = Intent(LOCAL_NOTIFICATION)
                                .putExtra("id", id)
                                .putExtra("eventName", event.eventName)
                                .putExtra("eventStart", false)
                                .putExtra("startTime", eventTime)

                            val pendingIntent = PendingIntent.getBroadcast(
                                context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP, eventTime, pendingIntent)
                            alarmList.add(alarmIntent)

                            val timeFormat =
                                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
                            Log.d(
                                TAG,
                                "Alarm with id ${id.toString()} and startTime ${
                                    timeFormat.format(Date(eventTime))
                                } created for ${event.eventName}"
                            )
                        }
                    }
                } else {
                    Log.d(TAG, "Clearing alarms for ${oldEvent.eventName}")
                    clearEventAlarms(oldEvent)
                    setAlarm(event)
                }

            } else if (event.startTime == null) {
                Log.w(TAG, "setAlarm was called for an event without a start time")
            } else {
                Log.w(TAG, "setAlarm was called without a user")
            }
        }

        //Clears all alarms for 1 event
        fun clearEventAlarms(event: Event) {
            if (event.startTime != null) {
                for (intent in alarmList) {
                    val eventStart = event.startTime.time
                    val eventEnd = eventStart + event.duration

                    val alarmTime = intent.getLongExtra("startTime", 0)
                    if (alarmTime == eventStart || alarmTime == eventEnd) {
                        val id = intent.getIntExtra("id", 0)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, id, intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        alarmManager.cancel(pendingIntent)
                        alarmList.remove(intent)
                    }
                }
            } else {
                Log.w(TAG, "Tried to delete alarm for an event without a start time")
            }
        }

        //clears either the start or end alarm of an event
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
            Log.d(TAG, "Setting all alarms")
            for (event in eventList)
            {
                if (event.startTime != null)
                    setAlarm(event)
            }
        }

        private fun clearAllAlarms() {
            Log.d(TAG, "Clearing all alarms")
            for (intent in alarmList)
            {
                val id = intent.getIntExtra("id", 0)
                val pendingIntent = PendingIntent.getBroadcast(context, id, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.cancel(pendingIntent)
            }
            alarmList.clear()
            Log.d(TAG, "Cleared alarmList")
        }

        private fun clearStartAlarms() {
            for (event in eventList)
            {
                if (event.startTime != null) {
                    clearOneAlarm(event, true)
                }
            }
        }

        private fun clearEndAlarms() {
            for (event in eventList)
            {
                if (event.startTime != null) {
                    clearOneAlarm(event, false)
                }
            }
        }

        fun resetAlarms() {
            clearAllAlarms()
            setAllAlarms()
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

