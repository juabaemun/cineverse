package com.example.cineversemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cineversemobile.data.local.AppDatabase
import com.example.cineversemobile.model.Movie
import com.example.cineversemobile.model.Screening
import com.example.cineversemobile.ui.screens.*
import com.example.cineversemobile.ui.theme.CineVerseMobileTheme
import com.example.cineversemobile.ui.viewmodel.MovieViewModel
import com.example.cineversemobile.util.AppConfig

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cineverse-db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val movieViewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val appConfig = remember { AppConfig(context) }

            // Cargamos la IP guardada al iniciar
            LaunchedEffect(Unit) {
                movieViewModel.initConfig(appConfig)
            }

            CineVerseMobileTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF09090B)) {

                    // Lógica de "Muro de Conexión"
                    if (movieViewModel.apiUrl.isBlank() || movieViewModel.showConnectionError) {
                        ConnectionErrorDialog(movieViewModel)
                    } else {
                        val navController = rememberNavController()

                        NavHost(navController = navController, startDestination = "login") {

                            composable("login") {
                                LoginScreen(viewModel = movieViewModel, onLoginSuccess = {
                                    navController.navigate("billboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                })
                            }

                            composable("billboard") {
                                MovieBillboardScreen(
                                    movieViewModel = movieViewModel,
                                    onMovieClick = { screening ->
                                        movieViewModel.selectedScreening = screening
                                        navController.navigate("seats")
                                    },
                                    onViewTickets = { navController.navigate("my_tickets") },
                                    onViewSupport = { navController.navigate("support") },
                                    onLogout = {
                                        movieViewModel.logout {
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            composable("seats") {
                                val screening = movieViewModel.selectedScreening
                                if (screening != null) {
                                    SeatSelectionScreen(
                                        screeningId = screening.id,
                                        movieTitle = screening.movie.title,
                                        basePrice = screening.price,
                                        viewModel = movieViewModel,
                                        room = screening.room,
                                        onConfirm = { seats, total ->
                                            movieViewModel.confirmPurchase(
                                                screening.id,
                                                screening.movie.title,
                                                seats,
                                                total,
                                                db
                                            )
                                            navController.navigate("my_tickets")
                                        }
                                    )
                                } else {
                                    LaunchedEffect(Unit) { navController.popBackStack() }
                                }
                            }

                            composable("my_tickets") {
                                MyTicketsScreen(database = db, viewModel = movieViewModel)
                            }

                            composable("support") {
                                SupportChatScreen(viewModel = movieViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo que solicita la IP al usuario si la conexión falla o está vacía.
 */
@Composable
fun ConnectionErrorDialog(viewModel: MovieViewModel) {
    var ipInput by remember { mutableStateOf(viewModel.apiUrl) }

    AlertDialog(
        onDismissRequest = { },
        title = { Text("Configuración de Servidor") },
        text = {
            Column {
                Text("Introduce la URL del backend (Ej: http://10.0.2.2:8080/):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("URL de la API") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (ipInput.isNotBlank()) viewModel.updateApiUrl(ipInput)
            }) {
                Text("Conectar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieBillboardScreen(
    movieViewModel: MovieViewModel,
    onMovieClick: (Screening) -> Unit,
    onViewTickets: () -> Unit,
    onViewSupport: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        movieViewModel.fetchScreenings()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Deseas salir, ${movieViewModel.currentUser?.displayName ?: "Usuario"}?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("SALIR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("CANCELAR") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CINEVERSE", fontWeight = FontWeight.Black, color = Color(0xFFFFE81F))
                        movieViewModel.currentUser?.let {
                            Text("Hola, ${it.displayName}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onViewSupport) {
                        Icon(Icons.Default.MailOutline, null, tint = Color.White)
                    }
                    IconButton(onClick = onViewTickets) {
                        Icon(Icons.Default.List, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF09090B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (movieViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFFE81F))
            } else if (movieViewModel.screenings.isEmpty()) {
                Text(
                    text = "No hay sesiones disponibles hoy",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movieViewModel.screenings) { screening ->
                        MovieCard(
                            movie = screening.movie,
                            price = screening.price,
                            onClick = { onMovieClick(screening) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, price: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.imageUrl)
                    .setHeader("User-Agent", "Mozilla/5.0")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = movie.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "${String.format("%.2f", price)}€", color = Color(0xFFFFE81F), fontSize = 14.sp)
                Text(text = movie.synopsis, color = Color.LightGray, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}