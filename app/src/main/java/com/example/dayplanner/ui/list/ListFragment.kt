package com.example.dayplanner.ui.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.dayplanner.MainActivity.Companion.listAdapterPosition
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentListAddBinding
import com.example.dayplanner.databinding.FragmentPlannerBinding
import com.example.dayplanner.ui.planner.PlannerAdapter
import com.example.dayplanner.ui.planner.PlannerItem
import com.example.dayplanner.ui.planner.PlannerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList


open class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateRecyclerView()
        val fab = binding.floatingActionButton
        fab.show()
        fab.setOnClickListener() {
            val action = ListFragmentDirections.actionNavigationListToNavigationListAdd()
            findNavController().navigate(action)
        }
        return root
    }

    fun updateRecyclerView() {
        mRecyclerView = binding.listEvents
        mRecyclerView.adapter = ListAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        if (listAdapterPosition != -1) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val db = Firebase.firestore
                val event = eventList.get(listAdapterPosition)
                db.collection("Users/${uid}/events")
                    .whereEqualTo("eventName", event.eventName)
                    .whereEqualTo("startTime", event.startTime)
                    .whereEqualTo("duration", event.duration)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            db.collection("Users/${uid}/events")
                                .document(document.id)
                                .delete()
                        }
                    }
            }
            eventList.removeAt(listAdapterPosition)
            mRecyclerView.adapter?.notifyItemRemoved(listAdapterPosition)
            listAdapterPosition = -1 // reset the value to default
        }
        updateRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}