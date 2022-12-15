package com.example.dayplanner.ui.list

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dayplanner.databinding.FragmentListBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.MainActivity.Companion.appWasJustStarted
import com.example.dayplanner.MainActivity.Companion.evtListFileName
import com.example.dayplanner.MainActivity.Companion.listAdapterPosition
import com.example.dayplanner.MainActivity.Companion.numEventListProperties
import com.example.dayplanner.background.Alarms
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.eventList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.InputStream
import java.util.*


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

        // This is the default (initial) page. Handle user sign in with splash screen
        if (appWasJustStarted) {
            if (FirebaseAuth.getInstance().currentUser != null) {
                val action = ListFragmentDirections.actionNavigationListToNavigationSplashScreen()
                findNavController().navigate(action)
            } else {
                // NEW: Get data from internal storage
                val directory = context?.filesDir!!
                val eventFile: File = File(directory, evtListFileName)
                if (eventFile.exists()) {
                    val lineList = eventFile.readLines()
                    var line = 0
                    val maxLine = lineList.size
                    if (maxLine % numEventListProperties != 0) {
                        Toast.makeText(context, "Error: could not load events from storage.", Toast.LENGTH_LONG).show()
                    } else {
                        while (line + numEventListProperties <= maxLine) {
                            val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                            val startTime: Date = format.parse(lineList[line])
                            val duration: Int = lineList[line+1].toInt()
                            val eventName = lineList[line+2]
                            val location = lineList[line+3]
                            val recurring: Int = lineList[line+4].toInt()

                            eventList.add(Event(startTime, duration, eventName, location, recurring))
                            line += numEventListProperties
                        }
                    }
                }
                appWasJustStarted = false
            }
        }

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
                val event = eventList[listAdapterPosition]
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
                Alarms.clearEventAlarms(event)
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