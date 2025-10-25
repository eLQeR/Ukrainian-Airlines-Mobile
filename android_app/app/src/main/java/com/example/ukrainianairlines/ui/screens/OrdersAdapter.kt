package com.example.ukrainianairlines.ui.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.data.model.Order
import java.text.SimpleDateFormat
import java.util.*

class OrdersAdapter(private val onOrderClick: (Order) -> Unit) :
    ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val orderIdText: TextView = itemView.findViewById(R.id.order_id_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val ticketsText: TextView = itemView.findViewById(R.id.tickets_text)
        private val statusText: TextView = itemView.findViewById(R.id.status_text)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOrderClick(getItem(position))
                }
            }
        }

        fun bind(order: Order) {
            // Order ID
            orderIdText.text = "Order #${order.id}"

            // Created date
            order.created_at?.let {
                dateText.text = dateFormat.format(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .parse(it) ?: Date()
                )
            }

            // Tickets count
            ticketsText.text = "${order.tickets.size} ticket(s)"

            // Status
            statusText.text = if (order.is_cancelled) "Cancelled" else "Active"
            statusText.setTextColor(
                itemView.context.getColor(
                    if (order.is_cancelled) android.R.color.holo_red_dark
                    else android.R.color.holo_green_dark
                )
            )
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}