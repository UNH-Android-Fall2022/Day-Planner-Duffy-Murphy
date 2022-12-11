package com.example.dayplanner.ui.planner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentPlannerBinding
import com.example.dayplanner.DB_PULL_COMPLETED
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        Possibly set a timer to run a bit later if the db pull is not completed
//        if (DB_PULL_COMPLETED || FirebaseAuth.getInstance().currentUser == null) {
//            updateRecyclerView()
//        } else {
//
//        }

        updateRecyclerView()

        return root
    }

    private fun updateRecyclerView() {

//        if (FirebaseAuth.getInstance().currentUser != null) {
//            while (!dbPullCompleted)
//                delay(100)
//        }

        val eventRecyclerList: ArrayList<PlannerItem> = ArrayList()
        val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
        val plannerList: ArrayList<Event> = ArrayList()

        for(item in eventList){
            if (item.startTime != null)
                plannerList.add(item)
        }

        //Test code
//        val testList: ArrayList<Event> = arrayListOf(
//            Event(Date(), 60000*15, "Event 1"),
//            Event(Date(Date().time + 60000 * 60), 60000*45, "Event 4"),
//            Event(null, 60000*15, "Event 2"),
//            Event(Date(), 60000*30, "Event 3"),
//            Event(Date(Date().time + 60000 * 60*5), 60000*45, "Event 5")
//        )
//
//        plannerList.addAll(testList)

        plannerList.sortBy { it.startTime }

        for(item in plannerList){
            if (item.startTime != null) {
                eventRecyclerList.add(
                    PlannerItem(
                        timeFormat.format(item.startTime),
                        timeFormat.format(Date(item.startTime.time + item.duration)),
                        item.eventName,
                        item.duration,
                        item.location
                    )
                )
            }
        }

        mRecyclerView = binding.plannerEvents
        mRecyclerView.adapter = PlannerAdapter(eventRecyclerList, this)
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}