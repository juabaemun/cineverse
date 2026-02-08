package com.example.cineversemobile.model

data class Seat(
    val id: Int,
    val row: String,
    val number: Int,
    var isAvailable: Boolean = true,
    var isSelected: Boolean = false
)