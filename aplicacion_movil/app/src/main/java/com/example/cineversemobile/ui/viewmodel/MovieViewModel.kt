package com.example.cineversemobile.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cineversemobile.data.local.AppDatabase
import com.example.cineversemobile.data.network.*
import com.example.cineversemobile.model.ChatMessage
import com.example.cineversemobile.model.MessageType
import com.example.cineversemobile.model.Screening
import com.example.cineversemobile.model.Ticket
import com.example.cineversemobile.util.AppConfig
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MovieViewModel : ViewModel() {

    // --- CONFIGURACIÓN DINÁMICA DE IP (Añadido) ---
    var apiUrl by mutableStateOf("")
    var showConnectionError by mutableStateOf(false)
    private var appConfig: AppConfig? = null

    // --- ESTADO GLOBAL ---
    var screenings by mutableStateOf<List<Screening>>(emptyList())
    var isLoading by mutableStateOf(false)
    var currentUser by mutableStateOf<AuthResponse?>(null)
    var userEmail by mutableStateOf("")
    var authError by mutableStateOf<String?>(null)
    var selectedScreening by mutableStateOf<Screening?>(null)
    var occupiedSeats by mutableStateOf<List<String>>(emptyList())

    // --- ESTADO DEL CHAT ---
    val chatMessages = mutableStateListOf<ChatMessage>()
    private var stompManager: StompManager? = null

    // Helper para obtener Retrofit con la URL actual
    fun api() = RetrofitClient.getService(apiUrl)

    /**
     * Inicialización con AppConfig para recuperar la IP guardada.
     */
    fun initConfig(config: AppConfig) {
        this.appConfig = config
        val savedUrl = config.getApiUrl()
        if (savedUrl.isBlank()) {
            this.showConnectionError = true
        } else {
            this.apiUrl = savedUrl
            Log.d("CONFIG", "API URL cargada: $apiUrl")
        }
    }

    /**
     * Actualiza la URL y la persiste.
     */
    fun updateApiUrl(newUrl: String) {
        var formattedUrl = newUrl.trim()

        // Si el usuario no puso http:// ni https://, lo añadimos nosotros
        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            formattedUrl = "http://$formattedUrl"
        }

        // Aseguramos que termine en / para que Retrofit no de errores de path
        if (!formattedUrl.endsWith("/")) {
            formattedUrl = "$formattedUrl/"
        }

        Log.d("CONFIG", "URL formateada correctamente: $formattedUrl")

        this.apiUrl = formattedUrl
        this.showConnectionError = false
        appConfig?.saveApiUrl(formattedUrl)
    }

    /**
     * Inicia la conexión WebSocket STOMP.
     */
    fun startChat() {
        if (userEmail.isBlank() || apiUrl.isBlank()) return

        // Convertimos la URL de la API (http) a formato WebSocket (ws)
        // Ejemplo: http://3.233.229.97:8080/ -> ws://3.233.229.97:8080/ws/websocket
        val wsUrl = apiUrl.replace("http://", "ws://")
            .replace("https://", "wss://")
            .let { if (it.endsWith("/")) "${it}ws/websocket" else "$it/ws/websocket" }

        if (stompManager == null) {
            // Pasamos wsUrl al constructor del StompManager
            stompManager = StompManager(userEmail, wsUrl) { message ->
                if (!message.sender.equals(userEmail, ignoreCase = true)) {
                    chatMessages.add(message)
                }
            }
            stompManager?.connect()
        }
    }

    /**
     * Envía un mensaje usando la variable userEmail.
     */
    fun sendMessage(text: String) {
        if (userEmail.isBlank()) return

        val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val newMessage = ChatMessage(
            sender = userEmail,
            content = text,
            timestamp = now,
            type = MessageType.CHAT
        )

        chatMessages.add(newMessage)
        stompManager?.sendMessage(text)
    }

    /**
     * Cierra la sesión y limpia todo.
     */
    fun logout(onNavigateToLogin: () -> Unit) {
        stompManager?.disconnect()
        stompManager = null
        chatMessages.clear()
        currentUser = null
        userEmail = ""
        screenings = emptyList()
        authError = null
        onNavigateToLogin()
    }

    // --- AUTENTICACIÓN ---

    fun login(emailInput: String, pass: String, onShowBillboard: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                // CAMBIADO: Ahora usa api() en lugar de RetrofitClient.instance
                val response = api().login(AuthRequest(emailInput, pass))

                if (response.isSuccessful && response.body() != null) {
                    currentUser = response.body()
                    userEmail = emailInput
                    Log.d("AUTH_DEBUG", "Login OK. Usuario: $userEmail")
                    authError = null
                    showConnectionError = false
                    onShowBillboard()
                } else {
                    authError = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                Log.e("Auth", "Error: ${e.message}")
                showConnectionError = true // Dispara el diálogo de IP
                authError = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(
        username: String,
        email: String,
        pass: String,
        confirmPass: String,
        onRegisterSuccess: () -> Unit
    ) {
        if (pass != confirmPass) {
            authError = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    nombreReal = username.trim(),
                    email = email.trim(),
                    password = pass
                )
                // CAMBIADO: Ahora usa api()
                val response = api().register(request)

                if (response.isSuccessful) {
                    authError = null
                    onRegisterSuccess()
                } else {
                    authError = "Error: El email ya está registrado"
                }
            } catch (e: Exception) {
                Log.e("Auth", "Error registro: ${e.message}")
                showConnectionError = true
            }
        }
    }

    // --- LÓGICA DE NEGOCIO ---

    fun fetchScreenings() {
        val token = currentUser?.token ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                // CAMBIADO: Ahora usa api()
                val result = api().getAllScreenings("Bearer $token")
                screenings = result
                showConnectionError = false
                Log.d("API", "Screenings cargados: ${result.size}")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error cargando screenings: ${e.message}")
                showConnectionError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchOccupiedSeats(screeningId: Long) {
        val token = currentUser?.token ?: return
        viewModelScope.launch {
            try {
                val seats = api().getOccupiedSeats("Bearer $token", screeningId)
                occupiedSeats = seats
                showConnectionError = false
            } catch (e: Exception) {
                Log.e("API", "Error asientos: ${e.message}")
                showConnectionError = true
            }
        }
    }

    fun confirmPurchase(
        screeningId: Long,
        movieTitle: String,
        selectedSeats: List<String>,
        totalPrice: Double,
        database: AppDatabase
    ) {
        val token = currentUser?.token ?: return
        viewModelScope.launch {
            try {
                var allSuccessful = true
                selectedSeats.forEach { seatLabel ->
                    // CAMBIADO: Ahora usa api()
                    val response = api().reserveSeat(
                        "Bearer $token",
                        CineVerseApi.BookingRequest(screeningId, seatLabel)
                    )
                    if (!response.isSuccessful) allSuccessful = false
                }

                if (allSuccessful) {
                    syncTickets(database)
                }
            } catch (e: Exception) {
                Log.e("Purchase", "Error: ${e.message}")
                showConnectionError = true
            }
        }
    }

    fun syncTickets(database: AppDatabase) {
        val token = currentUser?.token ?: return
        val miEmail = userEmail

        viewModelScope.launch {
            try {
                val remoteBookings = api().getMyBookings("Bearer $token")

                val misEntradas = remoteBookings.filter { it.userEmail.equals(miEmail, ignoreCase = true) }

                database.ticketDao().deleteAllTickets()

                misEntradas.forEach { remote ->
                    database.ticketDao().insertTicket(
                        Ticket(
                            serverId = remote.id,
                            movieTitle = remote.screening?.movie?.title ?: "Película",
                            seats = remote.seat,
                            totalPrice = remote.screening?.price ?: 0.0,
                            isValidated = remote.validated
                        )
                    )
                }
                showConnectionError = false
                Log.d("Sync", "Sincronización limpia: ${misEntradas.size} tickets guardados.")
            } catch (e: Exception) {
                Log.e("Sync", "Error: ${e.message}")
                showConnectionError = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stompManager?.disconnect()
    }
}