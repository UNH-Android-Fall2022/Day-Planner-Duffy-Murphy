package com.example.dayplanner.ui.list_add

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TimePicker
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.R
import com.example.dayplanner.databinding.FragmentListAddBinding


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
        val timePicker: TimePicker = binding.evtStartTime
        // TODO: Figure out how to let the user edit the timepicker.
//        timePicker.setOnTimeChangedListener() {
//
//        }
        spinnerStartTime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                spinnerStartTime.setSelection(pos)
                spinnerStartTime.tooltipText = parent.getItemAtPosition(pos).toString()
                if (spinnerStartTime.tooltipText == "Yes") {
                    timePicker.visibility = View.VISIBLE
                    timePicker.isEnabled = true // it still doesn't work

                } else {
                    timePicker.visibility = View.GONE
                    timePicker.isEnabled = true // it still doesn't work
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                spinnerStartTime.setSelection(0)
                spinnerStartTime.tooltipText = parent.getItemAtPosition(0).toString()
            }
        }

        val button = binding.listAddSubmit
        button.setOnClickListener() {
            val title = binding.evtTitle.text
            val duration = binding.evtDuration.text.toString()
            var startTime = ""
            if (timePicker.visibility == View.VISIBLE) {
                startTime = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            }
            val recurring = binding.spinnerRecurring.selectedItem.toString()
            val location = binding.evtLocation.text.toString()


            if (title.isEmpty()) {
                buildAlertDialog(context,"Missing Event Title", "Please enter an event title!")
            } else if (duration.isEmpty() || duration.toInt() > 24*60) {
                buildAlertDialog(
                    context,
                    "Invalid Event Duration",
                    "Please enter an event duration that is longer than 0 minutes and shorter than 24 hours!"
                )
            } else if (startTime.isEmpty() || startTime.toInt() > 24*60) {
                buildAlertDialog(
                    context,
                    "Invalid Start Time",
                    "Please enter a start time that is greater than 0 minutes and shorter than 24 hours!"
                )
            } else {
                val action = ListAddFragmentDirections.actionNavigationListAddToNavigationList()
                findNavController().navigate(action)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun buildAlertDialog(context: Context?, title: String, message: String) {
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