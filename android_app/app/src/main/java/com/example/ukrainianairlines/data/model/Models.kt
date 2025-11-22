package com.example.ukrainianairlines.data.model

import java.io.Serializable

data class Airport(
    val id: Int,
    val name: String,
    val closest_big_city: String
) : Serializable

data class Route(
    val id: Int? = null,
    val source: Any, // Can be Airport or String
    val destination: Any, // Can be Airport or String
    val distance: Float? = null
) : Serializable

data class AirplaneType(
    val id: Int,
    val name: String
) : Serializable

data class Airplane(
    val id: Int,
    val name: String,
    val rows: Int,
    val seats_in_row: Int,
    val airplane_type: AirplaneType,
    val image: String? = null,
    val capacity: Int = rows * seats_in_row
) : Serializable

data class Crew(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val avatar: String? = null
) : Serializable

data class Flight(
    val id: Int,
    val route: Route?,
    val airplane: Any?, // Can be Airplane or String
    val departure_time: String?,
    val arrival_time: String?,
    val crews: List<Crew> = emptyList(),
    val tickets_available: Int? = null,
    val capacity: Int? = null,
    val time_of_flight: String? = null
) : Serializable

data class Passenger(
    val id: Int? = null,
    val first_name: String,
    val last_name: String
) : Serializable

data class Ticket(
    val id: Int? = null,
    val row: Int,
    val seat: Int,
    val flight: Flight?,
    val passenger: Passenger
) : Serializable

data class Order(
    val id: Int? = null,
    val created_at: String? = null,
    val tickets: List<Ticket>,
    val is_cancelled: Boolean = false
) : Serializable

data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val password: String,
    val is_staff: Boolean = false
)

data class AuthTokens(
    val access: String,
    val refresh: String
)

data class ApiResponse<T>(
    val result: T? = null,
    val error: String? = null
)

data class FlightSearchResult(
    val directFlights: List<Flight> = emptyList(),
    val transferOptions: List<TransferOption> = emptyList(),
    val message: String? = null
)

data class TransferOption(
    val flights: List<Flight>
)