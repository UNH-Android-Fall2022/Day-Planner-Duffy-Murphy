package com.example.dayplanner.ui.list_add

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
        val spinner: Spinner = binding.spinner
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
                spinner.adapter = adapter
            }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                spinner.setSelection(pos)
                spinner.tooltipText = parent.getItemAtPosition(pos).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                spinner.setSelection(0)
                spinner.tooltipText = parent.getItemAtPosition(0).toString()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}