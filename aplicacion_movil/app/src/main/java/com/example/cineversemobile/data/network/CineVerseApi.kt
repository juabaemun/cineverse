package com.example.cineversemobile.data.network

import com.example.cineversemobile.model.Movie
import com.example.cineversemobile.model.Screening
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface CineVerseApi {
    @GET("api/movies")
    suspend fun getAllMovies(@Header("Authorization") token: String): List<Movie>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body user: RegisterRequest): Response<Any>

    @GET("api/bookings/occupied/{screeningId}")
    suspend fun getOccupiedSeats(
        @Header("Authorization") token: String,
        @Path("screeningId") screeningId: Long
    ): List<String>

    @POST("api/bookings/reserve")
    suspend fun reserveSeat(
        @Header("Authorization") token: String,
        @Body request: BookingRequest
    ): Response<Any>

    @GET("api/screenings")
    suspend fun getAllScreenings(@Header("Authorization") token: String): List<Screening>

    @GET("api/bookings/my-bookings")
    suspend fun getMyBookings(@Header("Authorization") token: String): List<BookingResponse>

    // Request DTOs
    data class BookingRequest(val screeningId: Long, val seat: String)
}



data class BookingResponse(
    val id: Long,
    val validated: Boolean,
    val seat: String,
    val screening: ScreeningDTO?,
    val user: UserDTO? // Añadimos el objeto usuario que viene de Java
) {
    // Propiedad calculada para acceder fácil al email
    val userEmail: String? get() = user?.email
}

data class UserDTO(
    val email: String
)

data class ScreeningDTO(
    val price: Double,
    val movie: MovieDTO?
)

data class MovieDTO(
    val title: String
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val role: String,
    val displayName: String
)

data class RegisterRequest(
    @SerializedName("nombreReal") val nombreReal: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "CLIENT"
)

data class ReservationRequest(val movieTitle: String, val selectedSeats: List<Int>)