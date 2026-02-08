package com.example.cineversemobile.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cineversemobile.ui.viewmodel.MovieViewModel

@Composable
fun SeatSelectionScreen(
    screeningId: Long,
    movieTitle: String,
    basePrice: Double,
    viewModel: MovieViewModel,
    // Mantenemos el objeto Room original para la capacidad y disposición
    room: com.example.cineversemobile.model.Room,
    onConfirm: (List<String>, Double) -> Unit
) {

    val occupiedSeats = viewModel.occupiedSeats
    val selectedSeats = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(screeningId) {
        isLoading = true
        try {
            // Llamamos a la función del ViewModel que centraliza la llamada a api()
            viewModel.fetchOccupiedSeats(screeningId)
        } catch (e: Exception) {
            Log.e("SeatSelection", "Error cargando asientos: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val totalToPay = selectedSeats.size * basePrice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CABECERA (Funcionalidad original intacta)
        Text(text = movieTitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Sala: ${room.name} | ${room.capacity} asientos", color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        // Representación de la pantalla del cine
        Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).background(Color(0xFFFFE81F)))
        Text("PANTALLA", color = Color(0xFFFFE81F), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFFE81F))
            }
        } else {
            // GRID DE ASIENTOS (Usa room.seatsPerRow y room.capacity originales)
            LazyVerticalGrid(
                columns = GridCells.Fixed(room.seatsPerRow),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(room.capacity) { index ->
                    val seatLabel = (index + 1).toString()

                    // Comprobación de estado (Ocupado, Seleccionado o Libre)
                    val isOccupied = occupiedSeats.contains(seatLabel)
                    val isSelected = selectedSeats.contains(seatLabel)

                    val boxColor = when {
                        isOccupied -> Color(0xFF1F1F23) // Ocupado
                        isSelected -> Color(0xFFFFE81F) // Seleccionado
                        else -> Color(0xFF3F3F46)       // Libre
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = boxColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(enabled = !isOccupied) {
                                if (isSelected) selectedSeats.remove(seatLabel)
                                else selectedSeats.add(seatLabel)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOccupied) {
                            Text("X", color = Color.DarkGray, fontSize = 10.sp)
                        } else {
                            Text(
                                text = seatLabel,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTÓN DE CONFIRMACIÓN (Mantiene el cálculo de totalToPay)
        Button(
            onClick = { onConfirm(selectedSeats.toList(), totalToPay) },
            enabled = selectedSeats.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE81F))
        ) {
            Text(
                text = "RESERVAR POR ${String.format("%.2f", totalToPay)}€",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}