package com.example.ukrainianairlines.data.model

data class Airport(
    val id: Int,
    val name: String,
    val closest_big_city: String
)

data class Route(
    val id: Int,
    val source: Airport,
    val destination: Airport,
    val distance: Float
)

data class AirplaneType(
    val id: Int,
    val name: String
)

data class Airplane(
    val id: Int,
    val name: String,
    val rows: Int,
    val seats_in_row: Int,
    val airplane_type: AirplaneType,
    val image: String? = null,
    val capacity: Int = rows * seats_in_row
)

data class Crew(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val avatar: String? = null
)

data class Flight(
    val id: Int,
    val route: Route,
    val airplane: Airplane,
    val departure_time: String,
    val arrival_time: String,
    val crews: List<Crew> = emptyList(),
    val tickets_available: Int = 0,
    val capacity: Int = airplane.capacity,
    val time_of_flight: String? = null
)

data class Passenger(
    val id: Int? = null,
    val first_name: String,
    val last_name: String
)

data class Ticket(
    val id: Int? = null,
    val row: Int,
    val seat: Int,
    val flight: Flight,
    val passenger: Passenger
)

data class Order(
    val id: Int? = null,
    val created_at: String? = null,
    val tickets: List<Ticket>,
    val is_cancelled: Boolean = false
)

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