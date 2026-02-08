package com.cineverse.api.services;

import com.cineverse.api.entities.User;
import com.cineverse.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // MÉTODO NUEVO: Obtener todos los usuarios para el Dashboard
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        // 1. Validar si el email ya está en uso
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Error: El email ya está registrado.");
        }

        // 2. Encriptar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Guardar y retornar
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow();

        user.setUsername(userDetails.getNombreReal());

        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        // Opcional: Solo codifica si la password no es nula/vacía
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        // Verificamos si existe antes de borrar para evitar errores silenciosos
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}