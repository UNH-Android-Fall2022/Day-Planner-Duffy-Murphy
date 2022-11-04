package com.example.dayplanner.ui.planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentPlannerBinding

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val plannerViewModel =
            ViewModelProvider(this).get(PlannerViewModel::class.java)

        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        val root: View = binding.root
// TODO Fix this
//        val eventRecyclerList: ArrayList<PlannerItem> = ArrayList()
//
//        for(item in eventList){
//            eventRecyclerList.add(
//                PlannerItem(
//                    item.startTime,
//                    workout.name,
//                    workout.workoutTime.toString()+" Minutes"
//                )
//            )
//        }
//
//        mRecyclerView = binding.recyclerViewWorkouts
//        mRecyclerView.setHasFixedSize(true)
//        mRecyclerView.layoutManager = LinearLayoutManager(context)
//        mRecyclerView.adapter = WorkoutAdapter(workoutRecyclerList, this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}