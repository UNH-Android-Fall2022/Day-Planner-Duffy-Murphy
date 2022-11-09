package com.example.dayplanner.ui.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.FragmentListBinding
import com.example.dayplanner.ui.list_add.ListAddFragment
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentListAddBinding
import com.example.dayplanner.databinding.FragmentPlannerBinding
import com.example.dayplanner.ui.planner.PlannerAdapter
import com.example.dayplanner.ui.planner.PlannerItem
import com.example.dayplanner.ui.planner.PlannerViewModel
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val eventRecyclerList: ArrayList<ListItem> = ArrayList()
        val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

        val eventList: ArrayList<Event> = ArrayList()

        for(item in eventList) {
            if (item.startTime == null)
                eventRecyclerList.add(ListItem(item.eventName, item.duration))
        }

        mRecyclerView = binding.listEvents
        mRecyclerView.adapter = ListAdapter(eventRecyclerList, this)


        val fab = binding.floatingActionButton
        fab.show()
        fab.setOnClickListener() {
            val action = ListFragmentDirections.actionNavigationListToNavigationListAdd()
            findNavController().navigate(action)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}