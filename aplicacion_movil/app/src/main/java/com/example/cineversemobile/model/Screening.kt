package com.example.cineversemobile.model


data class Screening(
    val id: Long,
    val movie: Movie,
    val room: Room,
    val price: Double,
    val startTime: String
)

data class Room(
    val id: Long,
    val name: String,
    val capacity: Int,
    val rowsCount: Int,
    val seatsPerRow: Int
)