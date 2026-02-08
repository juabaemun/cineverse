package com.example.cineversemobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cineversemobile.data.local.AppDatabase
import com.example.cineversemobile.ui.viewmodel.MovieViewModel
import com.example.cineversemobile.util.QRGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(database: AppDatabase, viewModel: MovieViewModel) {
    // 1. Escuchamos los tickets de Room
    val tickets by database.ticketDao().getAllTickets().collectAsState(initial = emptyList())

    // 2. Sincronización automática al entrar
    LaunchedEffect(Unit) {
        viewModel.syncTickets(database)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("MIS ENTRADAS", color = Color(0xFFFFE81F), fontWeight = FontWeight.Black)
                },
                actions = {
                    IconButton(onClick = { viewModel.syncTickets(database) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = Color(0xFFFFE81F)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF09090B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        if (tickets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes entradas", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(tickets) { ticket ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- COLUMNA IZQUIERDA: INFORMACIÓN ---
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (ticket.isValidated) Color(0xFF14532D) else Color(0xFF713F12),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (ticket.isValidated) "VALIDADA" else "PENDIENTE",
                                            color = if (ticket.isValidated) Color.Green else Color.Yellow,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = ticket.movieTitle,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text("Butaca: ${ticket.seats}", color = Color.LightGray, fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "ID: #${ticket.serverId}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${String.format("%.2f", ticket.totalPrice)}€",
                                    color = Color(0xFFFFE81F),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }

                            // --- COLUMNA DERECHA: CÓDIGO QR ---
                            // Generamos el QR con el ID del servidor para que el portero valide
                            val qrBitmap = remember(ticket.serverId) {
                                QRGenerator.generate("TICKET_ID:${ticket.serverId}")
                            }

                            qrBitmap?.let {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "QR Validation",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}