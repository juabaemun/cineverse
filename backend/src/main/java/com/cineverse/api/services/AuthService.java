package com.cineverse.api.services;

import com.cineverse.api.entities.User;
import com.cineverse.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 🛡️ Seguridad: Si no viene rol o intentan trucarlo, forzamos CLIENT
        if (user.getRole() == null || user.getRole().equals("ADMIN")) {
            user.setRole("CLIENT");
        }

        return userRepository.save(user);
    }
}
