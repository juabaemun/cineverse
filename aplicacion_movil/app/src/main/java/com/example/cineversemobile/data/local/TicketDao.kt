package com.example.cineversemobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cineversemobile.model.Ticket
import kotlinx.coroutines.flow.Flow // Importante

@Dao
interface TicketDao {
    @Insert
    suspend fun insertTicket(ticket: Ticket)

    @Query("SELECT * FROM tickets ORDER BY id DESC")
    fun getAllTickets(): kotlinx.coroutines.flow.Flow<List<Ticket>>

    @Query("UPDATE tickets SET isValidated = :status WHERE serverId = :id")
    suspend fun updateValidation(id: Long, status: Boolean)

    @Query("DELETE FROM tickets")
    suspend fun deleteAllTickets()
}