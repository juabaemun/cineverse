package com.example.cineversemobile.model

data class Movie(
    val id: Long,
    val title: String,
    val duration: Int,
    val synopsis: String,
    val imageUrl: String,
    val specialEvent: Boolean,
    val price: Double = 9.50 // Añadimos un valor por defecto o el campo para que compile
)