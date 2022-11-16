package com.example.dayplanner.ui.list

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.dayplanner.R
import com.example.dayplanner.ui.list.ListFragment
import com.example.dayplanner.data.eventList

class ListAdapter (
    private val context: ListFragment
): RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.event_name)
        val buttonEdit: Button = itemView.findViewById(R.id.list_edit)
        val buttonRemove: Button = itemView.findViewById(R.id.list_remove)
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
            val visibility = holder.buttonEdit.visibility
            if (visibility == View.GONE) {
                holder.buttonEdit.visibility = View.VISIBLE
                holder.buttonRemove.visibility = View.VISIBLE
            } else {
                holder.buttonEdit.visibility = View.GONE
                holder.buttonRemove.visibility = View.GONE
            }
        }

        holder.buttonRemove.setOnClickListener {
            val context = holder.view.context
            var returnVal = -2 // Return values for some reason are -1 for "yes" and -2 for "no"
            // No need to set a listener for the no button, it can be the default value
            AlertDialog.Builder(context)
                .setTitle("Confirm Event Deletion")
                .setMessage("Are you sure you want to delete this event?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes") { _, whichButton ->
                    returnVal = whichButton
                }.setNegativeButton("No", null).show()

            if (returnVal == -1) {
                eventList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }
}