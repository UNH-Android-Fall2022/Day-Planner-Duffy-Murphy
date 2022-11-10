package com.example.dayplanner.ui.list_add

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.R
import com.example.dayplanner.TAG
import com.example.dayplanner.databinding.FragmentListAddBinding
import com.example.dayplanner.ui.planner.PlannerItem
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor


class ListAddFragment : Fragment() {

    private var _binding: FragmentListAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListAddBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // The following is adapted from https://developer.android.com/develop/ui/views/components/spinner
        val spinnerRecurring: Spinner = binding.spinnerRecurring
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.evt_recurring_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                //
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerRecurring.adapter = adapter
            }
        }
        spinnerRecurring.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                spinnerRecurring.setSelection(pos)
                spinnerRecurring.tooltipText = parent.getItemAtPosition(pos).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerRecurring.setSelection(0)
                spinnerRecurring.tooltipText = parent.getItemAtPosition(0).toString()
            }
        }


        val spinnerStartTime: Spinner = binding.spinnerStartTime
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.evt_recurring_options,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                //
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerStartTime.adapter = adapter
            }
        }

        var startTime: Date? = Date()
        val timeTextView = binding.evtStartTime
        // This listener is used below with the spinner
        // Adapted from https://stackoverflow.com/questions/55090855/kotlin-problem-timepickerdialog-ontimesetlistener-in-class-output-2-values-lo
        fun timePickerListener() =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                startTime = calendar.time
                timeTextView.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(startTime)
            }

        spinnerStartTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                spinnerStartTime.setSelection(pos)
                spinnerStartTime.tooltipText = parent.getItemAtPosition(pos).toString()
                if (spinnerStartTime.tooltipText == "Yes") {
                    val timePicker: TimePickerDialog = TimePickerDialog (context,
                        timePickerListener(),
                        Calendar.HOUR_OF_DAY,
                        Calendar.MINUTE,
                        false)
                    timePicker.show()
                    timeTextView.visibility = View.VISIBLE
                } else {
                    timeTextView.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerStartTime.setSelection(0)
                spinnerStartTime.tooltipText = parent.getItemAtPosition(0).toString()
            }
        }

        val button = binding.listAddSubmit
        button.setOnClickListener() {
            val title = binding.evtTitle.text.toString()
            // duration is in units of milliseconds for compatibility with the Date library
            val duration = binding.evtDuration.text.toString().toInt() * 60000

            if (binding.spinnerStartTime.tooltipText != "Yes") {
                startTime = null
            }
            val recurring = binding.spinnerRecurring.selectedItem.toString()
            val location = binding.evtLocation.text.toString()

            var isValidEvent = true

            // Validate events:
            // Check for empty title
            // Check for empty event duration, note that the user can only enter numbers.
            // Check if event duration is longer than 24 hours (this is not the app for scheduling such an event)
            // Check if the event overlaps with a pre-existing event.
            if (title.isEmpty()) {
                buildAlertDialog(context,"Missing Event Title", "Please enter an event title!")
                isValidEvent = false
            } else if (binding.evtDuration.toString().isEmpty() || duration > 24*60*60*1000) { // Don't allow events that are longer than 24 hours
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
                val event: Event = Event(startTime, duration, title) // title is already title.toString()
                eventList.add(event)

                val user = Firebase.auth.currentUser
                if (user != null)
                    Firebase.firestore.collection("Users/${user.uid}/events").add(event)
                        .addOnSuccessListener { Log.d(TAG, "Event successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document: ", e) }
            }
        }
        return root
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
            .setPositiveButton("Confirm",
                DialogInterface.OnClickListener { dialog, which ->
                    // Continue with delete operation
                    binding.evtTitle.setSelection(0)
                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_baseline_warning_24)
            .show()
    }
}