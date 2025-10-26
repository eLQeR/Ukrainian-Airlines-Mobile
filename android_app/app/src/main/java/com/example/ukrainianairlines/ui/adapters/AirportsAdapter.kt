package com.example.ukrainianairlines.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.data.model.Airport

class AirportsAdapter(private val onClick: (Airport) -> Unit) : ListAdapter<Airport, AirportsAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Airport>() {
            override fun areItemsTheSame(oldItem: Airport, newItem: Airport): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Airport, newItem: Airport): Boolean = oldItem == newItem
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.airport_name)
        private val cityText: TextView = itemView.findViewById(R.id.airport_city)
        fun bind(item: Airport) {
            nameText.text = item.name
            cityText.text = item.closest_big_city
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_airport, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

