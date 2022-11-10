package com.example.dayplanner.ui.list

import com.example.dayplanner.ui.list.ListFragment
import com.example.dayplanner.ui.list.ListItem
import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.dayplanner.R


class ListAdapter (
    private val eventList: ArrayList<ListItem>,
    private val context: ListFragment
): RecyclerView.Adapter<ListAdapter.ViewHolder>(){

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.event_name)
        val view: ConstraintLayout = itemView.findViewById(R.id.constraint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = eventList[position].eventName
//        holder.view.minHeight = duration / 10000

        holder.itemView.setOnClickListener {
            Log.d(ContentValues.TAG, "Position clicked is $position")

        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}