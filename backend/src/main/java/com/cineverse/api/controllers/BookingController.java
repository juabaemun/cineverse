package com.cineverse.api.controllers;

import com.cineverse.api.dto.BookingRequest;
import com.cineverse.api.entities.Booking;
import com.cineverse.api.entities.User;
import com.cineverse.api.repositories.BookingRepository;
import com.cineverse.api.repositories.UserRepository;
import com.cineverse.api.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingService bookingService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/reserve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reserve(@RequestBody BookingRequest request, Authentication auth) {
        try {
            User user = userRepository.findByEmail(auth.getName()).orElseThrow();
            return ResponseEntity.ok(bookingService.reserve(user.getId(), request.getScreeningId(), request.getSeat()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/occupied/{screeningId}")
    public ResponseEntity<List<String>> getOccupiedSeats(@PathVariable Long screeningId) {
        return ResponseEntity.ok(bookingRepository.findByScreening_Id(screeningId).stream()
                .map(b -> b.getSeat().trim())
                .collect(Collectors.toList()));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication auth) {
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("EMPLOYEE"));

        if (isStaff) return ResponseEntity.ok(bookingRepository.findAll());

        return userRepository.findByEmail(auth.getName())
                .map(user -> ResponseEntity.ok(bookingRepository.findByUser(user)))
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/screening/{screeningId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<Booking>> getBookingsByScreening(@PathVariable Long screeningId) {
        return ResponseEntity.ok(bookingRepository.findByScreening_Id(screeningId));
    }

    @PatchMapping("/validate/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<String> validateEntry(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (booking.isValidated()) return ResponseEntity.badRequest().body("Ya validada.");
                    booking.setValidated(true);
                    bookingRepository.save(booking);
                    return ResponseEntity.ok("Validada correctamente");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}