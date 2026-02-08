package com.example.cineversemobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cineversemobile.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatScreen(viewModel: MovieViewModel) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Obtenemos el email actual. Si es null por pérdida de estado, usamos el test
    val myEmail = viewModel.userEmail

    // 1. Iniciar conexión al entrar
    LaunchedEffect(Unit) {
        Log.d("STOMP_DEBUG", "Entrando en la pantalla de chat para: $myEmail")
        viewModel.startChat()
    }

    // 2. Scroll automático al recibir mensajes nuevos
    LaunchedEffect(viewModel.chatMessages.size) {
        if (viewModel.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SOPORTE CINEVERSE", color = Color(0xFFFFE81F), fontWeight = FontWeight.Bold)
                        Text("En línea", color = Color.Green, fontSize = 10.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF18181B))
            )
        },
        containerColor = Color(0xFF09090B)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Área de Mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(viewModel.chatMessages) { msg ->
                    // Log de depuración para verificar quién envía y quién recibe
                    Log.d("CHAT_UI", "Remitente msg: ${msg.sender} | Mi Email: $myEmail")

                    val isFromMe = msg.sender.equals(myEmail, ignoreCase = true)

                    ChatBubble(
                        text = msg.content,
                        isFromUser = isFromMe,
                        timestamp = msg.timestamp ?: "",
                        senderName = if (isFromMe) "Tú" else "Soporte CineVerse"
                    )
                }
            }

            // Barra de entrada inferior
            Surface(
                color = Color(0xFF18181B),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF27272A),
                            unfocusedContainerColor = Color(0xFF27272A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        },
                        containerColor = Color(0xFFFFE81F),
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isFromUser: Boolean, timestamp: String, senderName: String) {
    val bubbleColor = if (isFromUser) Color(0xFFFFE81F) else Color(0xFF27272A)
    val textColor = if (isFromUser) Color.Black else Color.White

    val shape = if (isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
    ) {
        // Etiqueta de nombre arriba de la burbuja (solo para el Administrador)
        if (!isFromUser) {
            Text(
                text = senderName,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Surface(
            color = bubbleColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                if (timestamp.isNotEmpty()) {
                    Text(
                        text = timestamp,
                        color = if (isFromUser) Color.DarkGray else Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}