package com.example.cineversemobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverId: Long, // ID del backend para poder preguntar si ya se validó
    val movieTitle: String,
    val seats: String,
    val totalPrice: Double,
    val isValidated: Boolean = false,
    val date: Long = System.currentTimeMillis()
)