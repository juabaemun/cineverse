package com.example.cineversemobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cineversemobile.model.Ticket

@Database(entities = [Ticket::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}