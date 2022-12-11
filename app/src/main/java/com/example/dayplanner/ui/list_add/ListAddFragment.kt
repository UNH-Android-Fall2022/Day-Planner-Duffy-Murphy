package com.example.dayplanner.ui.list_add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.*
import com.example.dayplanner.MainActivity.Companion.cameFromMapsActivity
import com.example.dayplanner.MainActivity.Companion.listAdapterPosition
import com.example.dayplanner.MainActivity.Companion.location
import com.example.dayplanner.background.Alarms
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentListAddBinding
import com.google.android.gms.maps.MapView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


//import com.example.dayplanner.ui.list_add.WorkAroundMapFragment
//import com.google.android.gms.maps.OnMapReadyCallback

class ListAddFragment() : Fragment() {

    private var _binding: FragmentListAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var mMap: MapView? = null
    // This var is set sometimes during onStart() so it needs bigger scope
    private var startTime: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListAddBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val switchRecurring: SwitchMaterial = binding.switchRecurring

//        // Populate with default values from an event if edit is pressed
//        // This changes the check state of the switch so it is called first
//        if (listAdapterPosition != -1) {
//            setup(listAdapterPosition)
//            // DO NOT reset the listAdapterPosition yet, as navigating back
//            // to the list fragment needs to update the recyclerView
////            listAdapterPosition = -1 // Reset
//        }

        switchRecurring.setOnClickListener {
            // Responds to switch being checked/unchecked
            if (switchRecurring.isChecked) {
                switchRecurring.text = "Yes"
            } else {
                switchRecurring.text = "No"
            }
        }

        val timeTextView = binding.evtStartTime
        // This listener is used below with the switch to allow setting the time
        // Adapted from https://stackoverflow.com/questions/55090855/kotlin-problem-timepickerdialog-ontimesetlistener-in-class-output-2-values-lo
        fun timePickerListener() =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0) // Not interested in seconds

                startTime = calendar.time
                timeTextView.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(startTime!!)
            }

        // TimePickerDialog event listener. Copied for when the user clicks on the time.
        val switchStartTime: SwitchMaterial = binding.switchStartTime
        switchStartTime.setOnClickListener {
            // Responds to switch being checked/unchecked
            if (switchStartTime.isChecked) {
                switchStartTime.text = "Yes"
                // showDialogPossible is to prevent accidental dialog popups on resume
                val calendar = Calendar.getInstance()
                val timePicker: TimePickerDialog = TimePickerDialog(
                    context,
                    timePickerListener(),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
                timeTextView.visibility = View.VISIBLE
            } else {
                switchStartTime.text = "No"
                timeTextView.visibility = View.GONE
            }
        }

        binding.evtStartTime.setOnClickListener() {
            // Get a fresh calendar instance
            val calendar = Calendar.getInstance()
            val timePicker: TimePickerDialog = TimePickerDialog(
                context,
                timePickerListener(),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePicker.show()
        }

        // Hide the map and location if location services are turned off.
        if (userData != null) {
            if (userData!!.locationServices) {
                binding.evtLocation.visibility = View.VISIBLE
                binding.evtLocationLink.visibility = View.VISIBLE
            } else {
                binding.evtLocation.visibility = View.GONE
                binding.evtLocationLink.visibility = View.GONE
            }
        }

        binding.evtLocationLink.setOnClickListener() {
            // TODO: Navigate to map fragment
            val intent = Intent(activity, MapsActivity::class.java)
            startActivity(intent)
        }

        val button = binding.listAddSubmit
        button.setOnClickListener() {
            val title = binding.evtTitle.text.toString()
            // duration is in units of milliseconds for compatibility with the Date library
            val durationString = binding.evtDuration.text.toString()
            var duration = 0
            if (durationString.isNotEmpty()) {
                duration = durationString.toInt() * 60000
            }

            if (!binding.switchStartTime.isChecked) {
                startTime = null
            }

            var recurring = 0
            if (binding.switchRecurring.isChecked) {
                recurring = 1
            }

            val evtLocation = binding.evtLocation.text.toString()

            var isValidEvent = true

            // Validate events:
            // Check for empty title
            // Check for empty event duration, note that the user can only enter numbers.
            // Check if event duration is longer than 24 hours (this is not the app for scheduling such an event)
            // Check if the event overlaps with a pre-existing event.
            if (title.isEmpty()) {
                buildAlertDialog(context,"Missing Event Title", "Please enter an event title!")
                isValidEvent = false
            } else if (binding.evtDuration.text.toString().isEmpty() || duration > 24*60*60*1000) { // Don't allow events that are longer than 24 hours
                buildAlertDialog(
                    context,
                    "Invalid Event Duration",
                    "Please enter an event duration that is longer than 0 minutes and shorter than 24 hours!"
                )
                isValidEvent = false
            } else if (startTime != null) {
                // Validate event times
                for (event in eventList) {
                    if (event.startTime != null) {
                        val eventStart = event.startTime.time
                        val eventEnd = event.startTime.time + event.duration
                        val currentStart = startTime!!.time
                        val currentEnd = startTime!!.time + duration
                        // I made sure that startTime is not null but I was still forced to add the "!!"
                        // I do not trust the range check that the compiler suggests
                        if (eventStart < currentStart && eventEnd > currentStart) {
                            // This event has a start time that is contained within the duration of a pre-existing event
                            val startDelay: Int = ((eventEnd - currentStart) / 60000).toInt() + 1
                            buildAlertDialog(context,"Time Conflict",
                                "There is a time conflict with this event and other event(s)! " +
                                        "This event would need to start " + startDelay.toString() + " minutes later.")
                            isValidEvent = false
                            break
                        } else if (eventStart < currentEnd && eventEnd > currentEnd) {
                            // This event has an end time that is contained within the duration of a pre-existing event
                            val speedUp: Int = ((currentEnd - eventStart) / 60000).toInt() + 1
                            buildAlertDialog(context,"Time Conflict",
                                "There is a time conflict with this event and other event(s)! " +
                                        "This event would need to end " + speedUp.toString() + " minutes sooner.")
                            isValidEvent = false
                            break
                        }
                    }
                }
            }

            if (isValidEvent) {
                val action = ListAddFragmentDirections.actionNavigationListAddToNavigationList()
                findNavController().navigate(action)
                val event: Event = Event(startTime, duration, title, evtLocation, recurring) // title is already title.toString()
                eventList.add(event)

                // Clear local cache of fields, can be blocking
                context?.deleteSharedPreferences("ListAdd");

                val user = Firebase.auth.currentUser
                if (user != null) {
                    Firebase.firestore.collection("Users/${user.uid}/events").add(event)
                        .addOnSuccessListener { Log.d(TAG, "Event successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document: ", e) }
                    Alarms.setAlarm(event)
                }
            }
        }
        return root
    }

    // We try to call this function before the onCheckedChangeListener
    private fun setup(eventListPosition: Int) {
        val event = eventList[eventListPosition]
        if (event != null) {
            // The comment below is not needed because ListFragment.onResume() will take care of
            // the deletion
//            eventList.removeAt(listAdapterPosition)
            if (event.startTime != null) {
                binding.evtStartTime.text = DateFormat.getTimeInstance(DateFormat.SHORT)
                    .format(event.startTime)
                startTime = event.startTime
                binding.switchStartTime.text = "Yes"
                binding.switchStartTime.isChecked = true
                binding.evtStartTime.visibility = View.VISIBLE
            }
            if (event.recurring != 0) {
                binding.switchRecurring.text = "Yes"
                binding.switchRecurring.isChecked = true
            }
            binding.evtTitle.setText(event.eventName)
            binding.evtDuration.setText((event.duration / 60000).toString())
            binding.evtLocation.setText(event.location)
        }
    }

    // TODO: Handle full storage
    override fun onStop() {
        super.onStop()
        val sharedPrefs = context?.getSharedPreferences("ListAdd", Context.MODE_PRIVATE)
        sharedPrefs?.edit()?.putString("evtTitle", binding.evtTitle.text.toString())?.apply()
        sharedPrefs?.edit()?.putString("evtDuration", binding.evtDuration.text.toString())?.apply() // Numerical string
        sharedPrefs?.edit()?.putString("switchStartTime", binding.switchStartTime.text.toString())?.apply() // "Yes" or "No"
        sharedPrefs?.edit()?.putString("evtStartTime", binding.evtStartTime.text.toString())?.apply() // Start time from button
        sharedPrefs?.edit()?.putString("switchRecurring", binding.switchRecurring.text.toString())?.apply() // "Yes" or "No"
        sharedPrefs?.edit()?.putString("evtLocation", binding.evtLocation.text.toString())?.apply()
    }

    override fun onStart() {
        super.onStart()
        // Populate with existing values from an event when EDIT is pressed
        if (listAdapterPosition != -1) {
            setup(listAdapterPosition)
            // DO NOT reset the listAdapterPosition yet, as navigating back
            // to the list fragment needs to update the recyclerView
        } else if (cameFromMapsActivity) {
            // Load values from cache when navigating back from the MapsActivity
            val sharedPrefs = context?.getSharedPreferences("ListAdd", Context.MODE_PRIVATE)
            binding.evtTitle.setText(sharedPrefs?.getString("evtTitle", ""))
            binding.evtDuration.setText(sharedPrefs?.getString("evtDuration", ""))
            val startSwitchString = sharedPrefs?.getString("switchStartTime", "")
            if (startSwitchString == "Yes") {
                binding.switchStartTime.text = startSwitchString
                binding.switchStartTime.isChecked = true
                binding.evtStartTime.text = sharedPrefs.getString("evtStartTime", "")

                // Get the start time as a date
                // Adapted from https://stackoverflow.com/questions/5301226/convert-string-to-calendar-object-in-java
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
                calendar.time = sdf.parse(binding.evtStartTime.text.toString())!! // all done
                startTime = calendar.time

                binding.evtStartTime.visibility = View.VISIBLE
            } else {
                // This else additionally handles the case where the string is empty
                binding.switchStartTime.text = "No"
            }
            val recurringSwitchString = sharedPrefs?.getString("switchRecurring", "")
            if (recurringSwitchString == "Yes") {
                binding.switchRecurring.text = recurringSwitchString
                binding.switchRecurring.isChecked = true
            } else {
                binding.switchRecurring.text = "No"
            }

            // Handle information from map activity
            if (location != "") { // Location is companion object
                binding.evtLocation.setText(location)
                location = "" // Do this because it could autofill the next time list_add is opened
            } else {
                binding.evtLocation.setText(sharedPrefs?.getString("evtLocation", ""))
            }

            cameFromMapsActivity = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun buildAlertDialog(context: Context?, title: String, message: String) {
        // The following alertdialog is adapted from https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message) // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Confirm", null)
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_baseline_warning_24)
            .show()
    }
}
