package com.cineverse.api.services;

import com.cineverse.api.entities.Booking;
import com.cineverse.api.entities.Screening;
import com.cineverse.api.entities.User;
import com.cineverse.api.repositories.BookingRepository;
import com.cineverse.api.repositories.ScreeningRepository;
import com.cineverse.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingService {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private ScreeningRepository screeningRepository;
    @Autowired private UserRepository userRepository;

    @Transactional // 🛡️ Evita condiciones de carrera (dos personas reservando lo mismo)
    public Booking reserve(Long userId, Long screeningId, String seat) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        if (bookingRepository.existsByScreening_IdAndSeat(screeningId, seat)) {
            throw new RuntimeException("El asiento " + seat + " acaba de ser ocupado.");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setScreening(screening);
        booking.setSeat(seat.trim().toUpperCase()); // Normalizamos el asiento
        booking.setValidated(false);
        booking.setBookingDate(LocalDateTime.now());

        return bookingRepository.save(booking);
    }
}