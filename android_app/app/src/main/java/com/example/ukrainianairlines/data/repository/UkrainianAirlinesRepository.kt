package com.example.ukrainianairlines.data.repository

import com.example.ukrainianairlines.data.api.UkrainianAirlinesApi
import com.example.ukrainianairlines.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class UkrainianAirlinesRepository(private val tokenProvider: (() -> String?)? = null) {

    private fun getApi(): UkrainianAirlinesApi {
        val token = tokenProvider?.invoke()
        return UkrainianAirlinesApi.create(token)
    }

    // Authentication
    fun login(username: String, password: String): Flow<Result<AuthTokens>> = flow {
        try {
            val response = getApi().login(mapOf("username" to username, "password" to password))
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Login failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun register(user: User): Flow<Result<User>> = flow {
        try {
            val response = getApi().register(user)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Registration failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun refreshToken(refreshToken: String): Flow<Result<String>> = flow {
        try {
            val response = getApi().refreshToken(mapOf("refresh" to refreshToken))
            if (response.isSuccessful) {
                response.body()?.get("access")?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("No access token in response")))
            } else {
                emit(Result.failure(Exception("Token refresh failed: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Flights
    fun getFlights(
        sourceAirport: Int? = null,
        destinationAirport: Int? = null,
        departureDate: String? = null,
        route: Int? = null
    ): Flow<Result<List<Flight>>> = flow {
        try {
            val response = getApi().getFlights(sourceAirport, destinationAirport, departureDate, route)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get flights: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getFlight(flightId: Int): Flow<Result<Flight>> = flow {
        try {
            val response = getApi().getFlight(flightId)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get flight: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Airports
    fun getAirports(name: String? = null, city: String? = null): Flow<Result<List<Airport>>> = flow {
        try {
            val response = getApi().getAirports(name, city)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get airports: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getAirport(airportId: Int): Flow<Result<Airport>> = flow {
        try {
            val response = getApi().getAirport(airportId)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get airport: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Routes
    fun getRoutes(source: Int? = null, destination: Int? = null): Flow<Result<List<Route>>> = flow {
        try {
            val response = getApi().getRoutes(source, destination)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get routes: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Orders
    fun getOrders(): Flow<Result<List<Order>>> = flow {
        try {
            val response = getApi().getOrders()
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get orders: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun createOrder(order: Order): Flow<Result<Order>> = flow {
        try {
            val response = getApi().createOrder(order)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to create order: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getOrder(orderId: Int): Flow<Result<Order>> = flow {
        try {
            val response = getApi().getOrder(orderId)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to get order: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun cancelOrder(orderId: Int): Flow<Result<Order>> = flow {
        try {
            val response = getApi().cancelOrder(orderId)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to cancel order: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Flight search
    fun searchFlights(fromAirport: Int, toAirport: Int, date: String): Flow<Result<Map<String, Any>>> = flow {
        try {
            val response = getApi().searchFlights(fromAirport, toAirport, date)
            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response")))
            } else {
                emit(Result.failure(Exception("Failed to search flights: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}