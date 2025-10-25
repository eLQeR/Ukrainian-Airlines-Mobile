package com.example.ukrainianairlines.data.api

import com.example.ukrainianairlines.data.model.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface UkrainianAirlinesApi {

    // Authentication
    @POST("api/user/token/")
    suspend fun login(@Body credentials: Map<String, String>): Response<AuthTokens>

    @POST("api/user/register/")
    suspend fun register(@Body user: User): Response<User>

    @POST("api/user/token/refresh/")
    suspend fun refreshToken(@Body refreshToken: Map<String, String>): Response<Map<String, String>>

    // Flights
    @GET("api/airlines/flights/")
    suspend fun getFlights(
        @Query("source_airport") sourceAirport: Int? = null,
        @Query("destination_airport") destinationAirport: Int? = null,
        @Query("departure_date") departureDate: String? = null,
        @Query("route") route: Int? = null
    ): Response<List<Flight>>

    @GET("api/airlines/flights/{id}/")
    suspend fun getFlight(@Path("id") flightId: Int): Response<Flight>

    // Airports
    @GET("api/airlines/airports/")
    suspend fun getAirports(
        @Query("name") name: String? = null,
        @Query("city") city: String? = null
    ): Response<List<Airport>>

    @GET("api/airlines/airports/{id}/")
    suspend fun getAirport(@Path("id") airportId: Int): Response<Airport>

    // Routes
    @GET("api/airlines/routes/")
    suspend fun getRoutes(
        @Query("source") source: Int? = null,
        @Query("destination") destination: Int? = null
    ): Response<List<Route>>

    // Orders
    @GET("api/airlines/orders/")
    suspend fun getOrders(): Response<List<Order>>

    @POST("api/airlines/orders/")
    suspend fun createOrder(@Body order: Order): Response<Order>

    @GET("api/airlines/orders/{id}/")
    suspend fun getOrder(@Path("id") orderId: Int): Response<Order>

    @POST("api/airlines/orders/{id}/cancel/")
    suspend fun cancelOrder(@Path("id") orderId: Int): Response<Order>

    // Flight search with transfers
    @GET("api/airlines/get-ways/")
    suspend fun searchFlights(
        @Query("airport1") fromAirport: Int,
        @Query("airport2") toAirport: Int,
        @Query("date") date: String
    ): Response<Map<String, Any>>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000/" // For Android emulator
        // Use "http://192.168.1.100:8000/" for physical device (replace with your computer's IP)

        fun create(token: String? = null): UkrainianAirlinesApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                token?.let {
                    request.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(request.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UkrainianAirlinesApi::class.java)
        }
    }
}