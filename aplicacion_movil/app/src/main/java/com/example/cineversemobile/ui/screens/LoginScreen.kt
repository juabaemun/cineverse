package com.example.cineversemobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cineversemobile.ui.viewmodel.MovieViewModel

@Composable
fun LoginScreen(viewModel: MovieViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CINEVERSE",
            color = Color(0xFFFFE81F),
            fontSize = 40.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = if (isRegistering) "Crea tu cuenta" else "Bienvenido de nuevo",
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isRegistering) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; localError = null },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFE81F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFFFE81F)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFE81F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFFFE81F)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it; localError = null },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFE81F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFFFE81F)
            )
        )

        if (isRegistering) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it; localError = null },
                label = { Text("Repetir Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFE81F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFFFE81F)
                )
            )
        }

        val displayError = localError ?: viewModel.authError
        displayError?.let {
            Text(text = it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isBlank() || pass.isBlank()) {
                    localError = "Debes rellenar los campos"
                } else if (isRegistering && (username.isBlank() || confirmPass.isBlank())) {
                    localError = "Rellena todos los campos de registro"
                } else {
                    if (isRegistering) {
                        // LLAMADA POR POSICIÓN (Más segura si hay dudas con los nombres)
                        viewModel.register(
                            username,
                            email,
                            pass,
                            confirmPass,
                            {
                                isRegistering = false
                                localError = "Registro completado. ¡Inicia sesión!"
                            }
                        )
                    } else {
                        viewModel.login(email, pass, onLoginSuccess)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE81F), contentColor = Color.Black),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(text = if (isRegistering) "REGISTRARME" else "ENTRAR", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isRegistering = !isRegistering
            localError = null
            viewModel.authError = null
        }) {
            Text(
                text = if (isRegistering) "¿Ya tienes cuenta? Entra aquí" else "¿No tienes cuenta? Crea una",
                color = Color.Gray
            )
        }
    }
}