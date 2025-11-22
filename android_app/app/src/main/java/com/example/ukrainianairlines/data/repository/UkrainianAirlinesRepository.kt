package com.example.ukrainianairlines.data.repository

import android.util.Log
import com.example.ukrainianairlines.data.api.UkrainianAirlinesApi
import com.example.ukrainianairlines.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class UkrainianAirlinesRepository(private val tokenProvider: (() -> String?)? = null) {

    private val gson = Gson()

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

    // Aggressive normalization for backend responses with string fields
    private fun normalizeFlightJson(json: JsonElement): JsonElement {
        if (!json.isJsonObject) return json
        val obj = json.asJsonObject.deepCopy()

        // Helper to normalize a field to object if it's a string, with default structure
        fun normalizeFieldToAirportObject(parent: JsonObject, field: String) {
            if (parent.has(field)) {
                val el = parent[field]
                if (el != null && el.isJsonPrimitive && el.asJsonPrimitive.isString) {
                    val airportObj = JsonObject()
                    airportObj.addProperty("id", -1)
                    airportObj.addProperty("name", el.asString)
                    airportObj.addProperty("closest_big_city", el.asString)
                    parent.add(field, airportObj)
                }
            }
        }

        // Normalize airplane
        if (obj.has("airplane")) {
            val airplane = obj["airplane"]
            if (airplane != null && airplane.isJsonPrimitive && airplane.asJsonPrimitive.isString) {
                val airplaneObj = JsonObject()
                airplaneObj.addProperty("id", -1)
                airplaneObj.addProperty("name", airplane.asString)
                airplaneObj.addProperty("rows", 0)
                airplaneObj.addProperty("seats_in_row", 0)
                val airplaneTypeObj = JsonObject()
                airplaneTypeObj.addProperty("id", -1)
                airplaneTypeObj.addProperty("name", "")
                airplaneObj.add("airplane_type", airplaneTypeObj)
                airplaneObj.addProperty("image", "")
                airplaneObj.addProperty("capacity", 0)
                obj.add("airplane", airplaneObj)
            }
        }

        // Normalize route and its nested fields
        if (obj.has("route")) {
            val route = obj["route"]
            if (route != null && route.isJsonObject) {
                val routeObj = route.asJsonObject.deepCopy()
                normalizeFieldToAirportObject(routeObj, "source")
                normalizeFieldToAirportObject(routeObj, "destination")
                obj.add("route", routeObj)
            }
        }

        return obj
    }

    // Normalize all flights in paginated response
    private fun normalizePaginatedFlights(json: JsonElement): JsonElement {
        if (!json.isJsonObject) return json
        val obj = json.asJsonObject.deepCopy()
        if (obj.has("results") && obj["results"].isJsonArray) {
            val results = obj["results"].asJsonArray
            val normalizedResults = JsonArray()
            for (flight in results) {
                normalizedResults.add(normalizeFlightJson(flight))
            }
            obj.add("results", normalizedResults)
        }
        return obj
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
            Log.d(
                "UAR.Repository",
                "getFlights: calling /api/airlines/flights/ with params source=$sourceAirport destination=$destinationAirport date=$departureDate route=$route"
            )
            val response =
                getApi().getFlights(sourceAirport, destinationAirport, departureDate, route)
            try {
                Log.d(
                    "UAR.Repository",
                    "getFlights: HTTP ${response.code()} ${response.message()} body=${
                        response.body()?.toString()?.take(1000)
                    }"
                )
            } catch (e: Exception) {
                Log.w("UAR.Repository", "getFlights: failed to log response body", e)
            }

            if (response.isSuccessful) {
                val body = response.body()
                val flights: List<Flight> = when {
                    body == null -> emptyList()
                    body.isJsonArray -> {
                        // Normalize each flight JSON element to ensure nested objects exist where model expects them
                        val arr = body.asJsonArray
                        val list = mutableListOf<Flight>()
                        arr.forEach { elem ->
                            try {
                                val normalized = normalizeFlightJson(elem)
                                val flight: Flight = gson.fromJson(normalized, Flight::class.java)
                                list.add(flight)
                            } catch (e: Exception) {
                                Log.w(
                                    "UAR.Repository",
                                    "Failed to parse flight element, skipping",
                                    e
                                )
                            }
                        }
                        list
                    }

                    body.isJsonObject -> {
                        // If paginated, normalize all flights in results before parsing
                        val normalizedObj = normalizePaginatedFlights(body)
                        val obj = normalizedObj.asJsonObject
                        val arr = when {
                            obj.has("results") && obj.get("results").isJsonArray -> obj.getAsJsonArray("results")
                            obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                            obj.has("result") && obj.get("result").isJsonArray -> obj.getAsJsonArray("result")
                            else -> null
                        }
                        if (arr != null) gson.fromJson(
                            arr,
                            object : com.google.gson.reflect.TypeToken<List<Flight>>() {}.type
                        )
                        else {
                            // Maybe object represents a single flight
                            try {
                                val normalized = normalizeFlightJson(obj)
                                val single: Flight = gson.fromJson(normalized, Flight::class.java)
                                listOf(single)
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                    }

                    else -> emptyList()
                }

                emit(Result.success(flights))
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
            try {
                Log.d("UAR.Repository", "getFlight: HTTP ${response.code()} ${response.message()} body=${response.body()?.toString()?.take(1000)}")
            } catch (e: Exception) { Log.w("UAR.Repository", "getFlight: failed to log body", e) }
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    emit(Result.failure(Exception("Empty response")))
                } else {
                    try {
                        val normalized = normalizeFlightJson(body)
                        val flight: Flight = gson.fromJson(normalized, Flight::class.java)
                        emit(Result.success(flight))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
            } else {
                emit(Result.failure(Exception("Failed to get flight: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Airports
    fun getAirports(name: String? = null, city: String? = null): Flow<Result<List<Airport>>> =
        flow {
            try {
                val response: Response<JsonElement> = getApi().getAirports(name, city)
                Log.d(
                    "UAR.Repository",
                    "getAirports: HTTP ${response.code()} ${response.message()}"
                )
                val json = response.body()
                if (json == null) {
                    Log.d("UAR.Repository", "getAirports: empty body")
                } else {
                    try {
                        Log.d(
                            "UAR.Repository",
                            "getAirports: raw json=${json.toString().take(1000)}"
                        )
                    } catch (e: Exception) {
                        Log.w("UAR.Repository", "getAirports: failed to log large json", e)
                    }
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    val airports: List<Airport> = when {
                        body == null -> emptyList()
                        body.isJsonArray -> gson.fromJson(
                            body.asJsonArray,
                            object : TypeToken<List<Airport>>() {}.type
                        )

                        body.isJsonObject -> {
                            // try to extract "results" or "data" arrays
                            val obj = body.asJsonObject
                            val arr: JsonArray? = when {
                                obj.has("results") && obj.get("results").isJsonArray -> obj.getAsJsonArray(
                                    "results"
                                )

                                obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray(
                                    "data"
                                )

                                obj.entrySet().any { it.value.isJsonArray } -> obj.entrySet()
                                    .first { it.value.isJsonArray }.value.asJsonArray

                                else -> null
                            }
                            if (arr != null) gson.fromJson(
                                arr,
                                object : TypeToken<List<Airport>>() {}.type
                            ) else emptyList()
                        }

                        else -> emptyList()
                    }
                    Log.d("UAR.Repository", "getAirports: parsed airports count=${airports.size}")
                    emit(Result.success(airports))
                } else {
                    val msg = "Failed to get airports: ${response.message()}"
                    Log.e("UAR.Repository", msg)
                    emit(Result.failure(Exception(msg)))
                }
            } catch (e: Exception) {
                Log.e("UAR.Repository", "getAirports: exception", e)
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
            try {
                Log.d("UAR.Repository", "getOrders: HTTP ${response.code()} ${response.message()} raw=${response.body()?.toString()?.take(1000)}")
            } catch (e: Exception) {
                Log.w("UAR.Repository", "getOrders: failed to log response body", e)
            }

            if (response.isSuccessful) {
                val body = response.body()
                val orders: List<Order> = when {
                    body == null -> emptyList()
                    body.isJsonArray -> {
                        val arr = body.asJsonArray
                        val normalizedArr = JsonArray()
                        for (orderEl in arr) {
                            if (orderEl.isJsonObject) {
                                val orderObj = orderEl.asJsonObject.deepCopy()
                                if (orderObj.has("tickets") && orderObj.get("tickets").isJsonArray) {
                                    val tickets = orderObj.getAsJsonArray("tickets")
                                    val normalizedTickets = JsonArray()
                                    for (t in tickets) {
                                        if (t.isJsonObject) {
                                            val tObj = t.asJsonObject.deepCopy()
                                            if (tObj.has("flight")) {
                                                val f = tObj.get("flight")
                                                if (f != null) {
                                                    when {
                                                        f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                        f.isJsonPrimitive -> {
                                                            val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                            val minFlight = JsonObject()
                                                            minFlight.addProperty("id", flightId)
                                                            val routeObj = JsonObject()
                                                            val src = JsonObject()
                                                            src.addProperty("id", -1)
                                                            src.addProperty("name", "")
                                                            src.addProperty("closest_big_city", "")
                                                            val dst = JsonObject()
                                                            dst.addProperty("id", -1)
                                                            dst.addProperty("name", "")
                                                            dst.addProperty("closest_big_city", "")
                                                            routeObj.add("source", src)
                                                            routeObj.add("destination", dst)
                                                            routeObj.addProperty("distance", 0.0)
                                                            minFlight.add("route", routeObj)
                                                            val airplaneObj = JsonObject()
                                                            airplaneObj.addProperty("id", -1)
                                                            airplaneObj.addProperty("name", "")
                                                            airplaneObj.addProperty("rows", 0)
                                                            airplaneObj.addProperty("seats_in_row", 0)
                                                            val airplaneTypeObj = JsonObject()
                                                            airplaneTypeObj.addProperty("id", -1)
                                                            airplaneTypeObj.addProperty("name", "")
                                                            airplaneObj.add("airplane_type", airplaneTypeObj)
                                                            airplaneObj.addProperty("image", "")
                                                            airplaneObj.addProperty("capacity", 0)
                                                            minFlight.add("airplane", airplaneObj)
                                                            minFlight.addProperty("departure_time", "")
                                                            minFlight.addProperty("arrival_time", "")
                                                            tObj.add("flight", minFlight)
                                                        }
                                                    }
                                                }
                                            }
                                            normalizedTickets.add(tObj)
                                        } else normalizedTickets.add(t)
                                    }
                                    orderObj.add("tickets", normalizedTickets)
                                }
                                normalizedArr.add(orderObj)
                            } else normalizedArr.add(orderEl)
                        }
                        gson.fromJson(
                            normalizedArr,
                            object : TypeToken<List<Order>>() {}.type
                        )
                    }

                    body.isJsonObject -> {
                        val obj = body.asJsonObject
                        val arr = when {
                            obj.has("results") && obj.get("results").isJsonArray -> obj.getAsJsonArray("results")
                            obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                            obj.has("result") && obj.get("result").isJsonArray -> obj.getAsJsonArray("result")
                            else -> null
                        }
                        if (arr != null) {
                            val normalizedArr = JsonArray()
                            for (orderEl in arr) {
                                if (orderEl.isJsonObject) {
                                    val orderObj = orderEl.asJsonObject.deepCopy()
                                    if (orderObj.has("tickets") && orderObj.get("tickets").isJsonArray) {
                                        val tickets = orderObj.getAsJsonArray("tickets")
                                        val normalizedTickets = JsonArray()
                                        for (t in tickets) {
                                            if (t.isJsonObject) {
                                                val tObj = t.asJsonObject.deepCopy()
                                                if (tObj.has("flight")) {
                                                    val f = tObj.get("flight")
                                                    if (f != null) {
                                                        when {
                                                            f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                            f.isJsonPrimitive -> {
                                                                val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                                val minFlight = JsonObject()
                                                                minFlight.addProperty("id", flightId)
                                                                val routeObj = JsonObject()
                                                                val src = JsonObject()
                                                                src.addProperty("id", -1)
                                                                src.addProperty("name", "")
                                                                src.addProperty("closest_big_city", "")
                                                                val dst = JsonObject()
                                                                dst.addProperty("id", -1)
                                                                dst.addProperty("name", "")
                                                                dst.addProperty("closest_big_city", "")
                                                                routeObj.add("source", src)
                                                                routeObj.add("destination", dst)
                                                                routeObj.addProperty("distance", 0.0)
                                                                minFlight.add("route", routeObj)
                                                                val airplaneObj = JsonObject()
                                                                airplaneObj.addProperty("id", -1)
                                                                airplaneObj.addProperty("name", "")
                                                                airplaneObj.addProperty("rows", 0)
                                                                airplaneObj.addProperty("seats_in_row", 0)
                                                                val airplaneTypeObj = JsonObject()
                                                                airplaneTypeObj.addProperty("id", -1)
                                                                airplaneTypeObj.addProperty("name", "")
                                                                airplaneObj.add("airplane_type", airplaneTypeObj)
                                                                airplaneObj.addProperty("image", "")
                                                                airplaneObj.addProperty("capacity", 0)
                                                                minFlight.add("airplane", airplaneObj)
                                                                minFlight.addProperty("departure_time", "")
                                                                minFlight.addProperty("arrival_time", "")
                                                                tObj.add("flight", minFlight)
                                                            }
                                                        }
                                                    }
                                                }
                                                normalizedTickets.add(tObj)
                                            } else normalizedTickets.add(t)
                                        }
                                        orderObj.add("tickets", normalizedTickets)
                                    }
                                    normalizedArr.add(orderObj)
                                } else normalizedArr.add(orderEl)
                            }
                            gson.fromJson(
                                normalizedArr,
                                object : TypeToken<List<Order>>() {}.type
                            )
                        } else {
                            try {
                                val orderObj = obj.deepCopy()
                                if (orderObj.has("tickets") && orderObj.get("tickets").isJsonArray) {
                                    val tickets = orderObj.getAsJsonArray("tickets")
                                    val normalizedTickets = JsonArray()
                                    for (t in tickets) {
                                        if (t.isJsonObject) {
                                            val tObj = t.asJsonObject.deepCopy()
                                            if (tObj.has("flight")) {
                                                val f = tObj.get("flight")
                                                if (f != null) {
                                                    when {
                                                        f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                        f.isJsonPrimitive -> {
                                                            val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                            val minFlight = JsonObject()
                                                            minFlight.addProperty("id", flightId)
                                                            val routeObj = JsonObject()
                                                            val src = JsonObject()
                                                            src.addProperty("id", -1)
                                                            src.addProperty("name", "")
                                                            src.addProperty("closest_big_city", "")
                                                            val dst = JsonObject()
                                                            dst.addProperty("id", -1)
                                                            dst.addProperty("name", "")
                                                            dst.addProperty("closest_big_city", "")
                                                            routeObj.add("source", src)
                                                            routeObj.add("destination", dst)
                                                            routeObj.addProperty("distance", 0.0)
                                                            minFlight.add("route", routeObj)
                                                            val airplaneObj = JsonObject()
                                                            airplaneObj.addProperty("id", -1)
                                                            airplaneObj.addProperty("name", "")
                                                            airplaneObj.addProperty("rows", 0)
                                                            airplaneObj.addProperty("seats_in_row", 0)
                                                            val airplaneTypeObj = JsonObject()
                                                            airplaneTypeObj.addProperty("id", -1)
                                                            airplaneTypeObj.addProperty("name", "")
                                                            airplaneObj.add("airplane_type", airplaneTypeObj)
                                                            airplaneObj.addProperty("image", "")
                                                            airplaneObj.addProperty("capacity", 0)
                                                            minFlight.add("airplane", airplaneObj)
                                                            minFlight.addProperty("departure_time", "")
                                                            minFlight.addProperty("arrival_time", "")
                                                            tObj.add("flight", minFlight)
                                                        }
                                                    }
                                                }
                                            }
                                            normalizedTickets.add(tObj)
                                        } else normalizedTickets.add(t)
                                    }
                                    orderObj.add("tickets", normalizedTickets)
                                }
                                val single: Order = gson.fromJson(orderObj, Order::class.java)
                                listOf(single)
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                    }

                    else -> emptyList()
                }

                emit(Result.success(orders))
            } else {
                emit(Result.failure(Exception("Failed to get orders: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun createOrder(order: Order): Flow<Result<Order>> = flow {
        try {
            // Build request JSON matching backend schema: { tickets: [ { row, seat, flight: <pk>, passenger: { first_name, last_name } } ] }
            val reqObj = JsonObject()
            val ticketsArr = JsonArray()
            order.tickets.forEach { t ->
                val ticketObj = JsonObject()
                ticketObj.addProperty("row", t.row)
                ticketObj.addProperty("seat", t.seat)
                // flight should be primary key (int)
                val flightId = t.flight.id
                ticketObj.addProperty("flight", flightId)
                val passengerObj = JsonObject()
                passengerObj.addProperty("first_name", t.passenger.first_name)
                passengerObj.addProperty("last_name", t.passenger.last_name)
                ticketObj.add("passenger", passengerObj)
                ticketsArr.add(ticketObj)
            }
            reqObj.add("tickets", ticketsArr)

            try {
                Log.d("UAR.Repository", "createOrder: request=${gson.toJson(reqObj)}")
            } catch (e: Exception) { /* ignore logging failures */ }

            val response = getApi().createOrder(reqObj)
            try {
                Log.d("UAR.Repository", "createOrder: HTTP ${response.code()} ${response.message()} body=${gson.toJson(response.body())}")
            } catch (e: Exception) {
                Log.w("UAR.Repository", "createOrder: failed to log response body", e)
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    emit(Result.failure(Exception("Empty response")))
                } else {
                    try {
                        // body should be single order JSON
                        val obj = body.asJsonObject
                        // reuse single-order normalization from getOrders
                        val orderObj = obj.deepCopy()
                        if (orderObj.has("tickets") && orderObj.get("tickets").isJsonArray) {
                            val tickets = orderObj.getAsJsonArray("tickets")
                            val normalizedTickets = JsonArray()
                            for (t in tickets) {
                                if (t.isJsonObject) {
                                    val tObj = t.asJsonObject.deepCopy()
                                    if (tObj.has("flight")) {
                                        val f = tObj.get("flight")
                                        if (f != null) {
                                            when {
                                                f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                f.isJsonPrimitive -> {
                                                    val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                    val minFlight = JsonObject()
                                                    minFlight.addProperty("id", flightId)
                                                    val routeObj = JsonObject()
                                                    val src = JsonObject()
                                                    src.addProperty("id", -1)
                                                    src.addProperty("name", "")
                                                    src.addProperty("closest_big_city", "")
                                                    val dst = JsonObject()
                                                    dst.addProperty("id", -1)
                                                    dst.addProperty("name", "")
                                                    dst.addProperty("closest_big_city", "")
                                                    routeObj.add("source", src)
                                                    routeObj.add("destination", dst)
                                                    routeObj.addProperty("distance", 0.0)
                                                    minFlight.add("route", routeObj)
                                                    val airplaneObj = JsonObject()
                                                    airplaneObj.addProperty("id", -1)
                                                    airplaneObj.addProperty("name", "")
                                                    airplaneObj.addProperty("rows", 0)
                                                    airplaneObj.addProperty("seats_in_row", 0)
                                                    val airplaneTypeObj = JsonObject()
                                                    airplaneTypeObj.addProperty("id", -1)
                                                    airplaneTypeObj.addProperty("name", "")
                                                    airplaneObj.add("airplane_type", airplaneTypeObj)
                                                    airplaneObj.addProperty("image", "")
                                                    airplaneObj.addProperty("capacity", 0)
                                                    minFlight.add("airplane", airplaneObj)
                                                    // minimal times
                                                    minFlight.addProperty("departure_time", "")
                                                    minFlight.addProperty("arrival_time", "")
                                                    tObj.add("flight", minFlight)
                                                }
                                            }
                                        }
                                    }
                                    normalizedTickets.add(tObj)
                                } else normalizedTickets.add(t)
                            }
                            orderObj.add("tickets", normalizedTickets)
                        }
                        val created: Order = gson.fromJson(orderObj, Order::class.java)
                        emit(Result.success(created))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
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
            try { Log.d("UAR.Repository", "getOrder: HTTP ${response.code()} ${response.message()} body=${response.body()?.toString()?.take(1000)}") } catch (e: Exception) {}
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    emit(Result.failure(Exception("Empty response")))
                } else {
                    try {
                        val obj = body.asJsonObject.deepCopy()
                        if (obj.has("tickets") && obj.get("tickets").isJsonArray) {
                            val tickets = obj.getAsJsonArray("tickets")
                            val normalizedTickets = JsonArray()
                            for (t in tickets) {
                                if (t.isJsonObject) {
                                    val tObj = t.asJsonObject.deepCopy()
                                    if (tObj.has("flight")) {
                                        val f = tObj.get("flight")
                                        if (f != null) {
                                            when {
                                                f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                f.isJsonPrimitive -> {
                                                    val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                    val minFlight = JsonObject()
                                                    minFlight.addProperty("id", flightId)
                                                    val routeObj = JsonObject()
                                                    val src = JsonObject()
                                                    src.addProperty("id", -1)
                                                    src.addProperty("name", "")
                                                    src.addProperty("closest_big_city", "")
                                                    val dst = JsonObject()
                                                    dst.addProperty("id", -1)
                                                    dst.addProperty("name", "")
                                                    dst.addProperty("closest_big_city", "")
                                                    routeObj.add("source", src)
                                                    routeObj.add("destination", dst)
                                                    routeObj.addProperty("distance", 0.0)
                                                    minFlight.add("route", routeObj)
                                                    val airplaneObj = JsonObject()
                                                    airplaneObj.addProperty("id", -1)
                                                    airplaneObj.addProperty("name", "")
                                                    airplaneObj.addProperty("rows", 0)
                                                    airplaneObj.addProperty("seats_in_row", 0)
                                                    val airplaneTypeObj = JsonObject()
                                                    airplaneTypeObj.addProperty("id", -1)
                                                    airplaneTypeObj.addProperty("name", "")
                                                    airplaneObj.add("airplane_type", airplaneTypeObj)
                                                    airplaneObj.addProperty("image", "")
                                                    airplaneObj.addProperty("capacity", 0)
                                                    minFlight.add("airplane", airplaneObj)
                                                    minFlight.addProperty("departure_time", "")
                                                    minFlight.addProperty("arrival_time", "")
                                                    tObj.add("flight", minFlight)
                                                }
                                            }
                                        }
                                    }
                                    normalizedTickets.add(tObj)
                                } else normalizedTickets.add(t)
                            }
                            obj.add("tickets", normalizedTickets)
                        }
                        val single: Order = gson.fromJson(obj, Order::class.java)
                        emit(Result.success(single))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
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
            try { Log.d("UAR.Repository", "cancelOrder: HTTP ${response.code()} ${response.message()} body=${response.body()?.toString()?.take(1000)}") } catch (e: Exception) {}
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    emit(Result.failure(Exception("Empty response")))
                } else {
                    try {
                        val obj = body.asJsonObject.deepCopy()
                        if (obj.has("tickets") && obj.get("tickets").isJsonArray) {
                            val tickets = obj.getAsJsonArray("tickets")
                            val normalizedTickets = JsonArray()
                            for (t in tickets) {
                                if (t.isJsonObject) {
                                    val tObj = t.asJsonObject.deepCopy()
                                    if (tObj.has("flight")) {
                                        val f = tObj.get("flight")
                                        if (f != null) {
                                            when {
                                                f.isJsonObject -> tObj.add("flight", normalizeFlightJson(f))
                                                f.isJsonPrimitive -> {
                                                    val flightId = try { f.asInt } catch (e: Exception) { -1 }
                                                    val minFlight = JsonObject()
                                                    minFlight.addProperty("id", flightId)
                                                    val routeObj = JsonObject()
                                                    val src = JsonObject()
                                                    src.addProperty("id", -1)
                                                    src.addProperty("name", "")
                                                    src.addProperty("closest_big_city", "")
                                                    val dst = JsonObject()
                                                    dst.addProperty("id", -1)
                                                    dst.addProperty("name", "")
                                                    dst.addProperty("closest_big_city", "")
                                                    routeObj.add("source", src)
                                                    routeObj.add("destination", dst)
                                                    routeObj.addProperty("distance", 0.0)
                                                    minFlight.add("route", routeObj)
                                                    val airplaneObj = JsonObject()
                                                    airplaneObj.addProperty("id", -1)
                                                    airplaneObj.addProperty("name", "")
                                                    airplaneObj.addProperty("rows", 0)
                                                    airplaneObj.addProperty("seats_in_row", 0)
                                                    val airplaneTypeObj = JsonObject()
                                                    airplaneTypeObj.addProperty("id", -1)
                                                    airplaneTypeObj.addProperty("name", "")
                                                    airplaneObj.add("airplane_type", airplaneTypeObj)
                                                    airplaneObj.addProperty("image", "")
                                                    airplaneObj.addProperty("capacity", 0)
                                                    minFlight.add("airplane", airplaneObj)
                                                    minFlight.addProperty("departure_time", "")
                                                    minFlight.addProperty("arrival_time", "")
                                                    tObj.add("flight", minFlight)
                                                }
                                            }
                                        }
                                    }
                                    normalizedTickets.add(tObj)
                                } else normalizedTickets.add(t)
                            }
                            obj.add("tickets", normalizedTickets)
                        }
                        val single: Order = gson.fromJson(obj, Order::class.java)
                        emit(Result.success(single))
                    } catch (e: Exception) {
                        emit(Result.failure(e))
                    }
                }
            } else {
                emit(Result.failure(Exception("Failed to cancel order: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Flight search
    fun searchFlights(
        fromAirport: Int,
        toAirport: Int,
        date: String
    ): Flow<Result<Map<String, Any>>> = flow {
        try {
            Log.d(
                "UAR.Repository",
                "searchFlights: calling /api/airlines/flights/ with params airport1=$fromAirport airport2=$toAirport date=$date"
            )
            val response = getApi().searchFlights(fromAirport, toAirport, date)
            try {
                Log.d(
                    "UAR.Repository",
                    "searchFlights: HTTP ${response.code()} ${response.message()} body=${
                        gson.toJson(response.body()).take(1000)
                    }"
                )
            } catch (e: Exception) {
                Log.w("UAR.Repository", "searchFlights: failed to log response body", e)
            }
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
