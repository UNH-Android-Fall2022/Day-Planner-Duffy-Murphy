package com.example.dayplanner.ui.planner

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.R
import java.util.*


class PlannerAdapter (
    private val eventList: ArrayList<PlannerItem>,
    private val context: PlannerFragment
    ): RecyclerView.Adapter<PlannerAdapter.ViewHolder>(){

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val startTime: TextView = itemView.findViewById(R.id.start_time)
        val endTime: TextView = itemView.findViewById(R.id.end_time)
        val name: TextView = itemView.findViewById(R.id.event_name)
        val location: TextView = itemView.findViewById(R.id.event_location)
        val buttonMap: ImageButton = itemView.findViewById(R.id.planner_map_button)
        val view: ConstraintLayout = itemView.findViewById(R.id.constraint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.planner_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val (startTime, endTime, name, duration, location) = eventList[position]

        holder.startTime.text = startTime
        holder.endTime.text = endTime
        holder.name.text = name
        holder.location.text = location

        //Card height changes based on the duration of the event
        holder.view.minHeight = duration / 10000

        holder.itemView.setOnClickListener {
            val visibility = holder.buttonMap.visibility
            if (visibility == View.GONE) {
                holder.buttonMap.visibility= View.VISIBLE
            } else {
                holder.buttonMap.visibility = View.GONE
            }
        }

        holder.buttonMap.setOnClickListener {
            val theAddress = (holder.location.text as String).replace(" ", "+")
            val uri: String = String.format(Locale.getDefault(), "geo:0,0?q=${theAddress}")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}