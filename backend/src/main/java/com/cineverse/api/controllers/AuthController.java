package com.cineverse.api.controllers;

import com.cineverse.api.dto.AuthRequest;
import com.cineverse.api.entities.User;
import com.cineverse.api.repositories.UserRepository;
import com.cineverse.api.security.JwtTokenProvider;
import com.cineverse.api.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Definimos el mensaje genérico para ambos casos por seguridad y claridad
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Credenciales inválidas");

        try {
            // 1. Buscamos al usuario
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);

            // 2. Si no existe OR la contraseña no coincide...
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // ...devolvemos EXACTAMENTE lo mismo
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // 3. Si todo es correcto, generamos el éxito
            String token = tokenProvider.generateToken(user.getEmail(), user.getRole());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());
            response.put("displayName", user.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // En caso de error técnico (DB caída, etc.)
            Map<String, String> technicalError = new HashMap<>();
            technicalError.put("error", "Error de conexión con el servicio");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(technicalError);
        }
    }
}