package com.example.dayplanner.ui.planner

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.R


class PlannerAdapter (
    private val eventList: ArrayList<PlannerItem>,
    private val context: PlannerFragment
    ): RecyclerView.Adapter<PlannerAdapter.ViewHolder>(){

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val startTime: TextView = itemView.findViewById(R.id.start_time)
        val endTime: TextView = itemView.findViewById(R.id.end_time)
        val name: TextView = itemView.findViewById(R.id.event_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.planner_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        val (startTime, endTime, name) = eventList[position]
        //TODO("Implement image resource")
        holder.startTime.text = startTime
        holder.endTime.text = endTime
        holder.name.text = name

        holder.itemView.setOnClickListener {
            Log.d(ContentValues.TAG, "Position clicked is $position")

        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}