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
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class Alarms {
    companion object {
        val alarmManager: AlarmManager = MainActivity.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
                val id: Int = if (_id != null) _id else Random.nextInt(0, Int.MAX_VALUE - 1)

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

        //Must include old event to update event! Otherwise, this function has no way of knowing for sure it's the same event
        fun setAlarm(event: Event, oldEvent: Event? = null) {
            if (event.startTime != null && userData != null) {
                if (oldEvent == null) {
                    if (userData!!.startNotifications) {
                        val eventTime = event.startTime.time
                        if (Date(eventTime).after(Date())) {
                            val id = Random.nextInt(0, Int.MAX_VALUE - 1)

                            val alarmIntent = Intent(LOCAL_NOTIFICATION)
                                .putExtra("id", id)
                                .putExtra("eventName", event.eventName)
                                .putExtra("eventStart", true)
                                .putExtra("startTime", eventTime)

                            val pendingIntent = PendingIntent.getBroadcast(
                                MainActivity.context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

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
                            val id = Random.nextInt(0, Int.MAX_VALUE - 1)

                            val alarmIntent = Intent(LOCAL_NOTIFICATION)
                                .putExtra("id", id)
                                .putExtra("eventName", event.eventName)
                                .putExtra("eventStart", false)
                                .putExtra("startTime", eventTime)

                            val pendingIntent = PendingIntent.getBroadcast(
                                MainActivity.context, id, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

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
            Log.d(TAG, "Clearing event alarms")
            val deletedAlarms: ArrayList<Intent> = ArrayList()
            if (event.startTime != null) {
                for (intent in alarmList) {
                    val eventStart = event.startTime.time
                    val eventEnd = eventStart + event.duration

                    val alarmTime = intent.getLongExtra("startTime", 0)
                    if (alarmTime == eventStart || alarmTime == eventEnd) {
                        val id = intent.getIntExtra("id", 0)
                        val pendingIntent = PendingIntent.getBroadcast(
                            MainActivity.context, id, intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager.cancel(pendingIntent)
                        deletedAlarms.add(intent)
                        Log.d(TAG, "Cleared alarm")
                    }
                }
                alarmList.removeAll(deletedAlarms)
            } else {
                Log.w(TAG, "Tried to delete alarm for an event without a start time")
            }
        }

        //clears either the start or end alarm of an event
        private fun clearOneAlarm(event: Event, startAlarm: Boolean)
        {
            if (event.startTime != null) {
                val alarmTime = if (startAlarm) event.startTime.time else event.startTime.time + event.duration
                val deletedAlarms: ArrayList<Intent> = ArrayList()
                for (intent in alarmList) {
                    if (alarmTime == intent.getLongExtra("startTime", 0)) {
                        val id = intent.getIntExtra("id", 0)
                        val pendingIntent = PendingIntent.getBroadcast(MainActivity.context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.cancel(pendingIntent)
                        deletedAlarms.add(intent)
                    }
                }
                alarmList.removeAll(deletedAlarms)
            } else {
                Log.w(TAG, "Tried to delete alarm for an event without a start time")
            }
        }

        fun setAllAlarms() {
            Log.d(TAG, "Setting all alarms")
            for (event in eventList)
            {
                if (event.startTime != null)
                    setAlarm(event)
            }
        }

        fun clearAllAlarms() {
            Log.d(TAG, "Clearing all alarms")
            for (intent in alarmList)
            {
                val id = intent.getIntExtra("id", 0)
                val pendingIntent = PendingIntent.getBroadcast(
                    MainActivity.context, id, intent,
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
    }
}