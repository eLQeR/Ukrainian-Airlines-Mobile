package com.example.ukrainianairlines.data.model

data class PaginatedOrders(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Order>
)

