package com.example.ukrainianairlines.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.data.model.Route

class RoutesAdapter : ListAdapter<Route, RoutesAdapter.ViewHolder>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Route>() {
            override fun areItemsTheSame(oldItem: Route, newItem: Route): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Route, newItem: Route): Boolean = oldItem == newItem
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val routeText: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(item: Route) {
            routeText.text = "${item.source.name} → ${item.destination.name} (${item.distance} km)"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

