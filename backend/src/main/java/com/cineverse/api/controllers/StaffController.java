package com.cineverse.api.controllers;

import com.cineverse.api.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')") // Empleados y Admins pueden validar
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private BookingRepository bookingRepository;

    // REQUISITO: Validación y control de entradas
    @PatchMapping("/validate/{bookingId}")
    public ResponseEntity<String> validateBooking(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    if (booking.isValidated()) {
                        return ResponseEntity.badRequest().body("ERROR: Esta entrada ya ha sido usada.");
                    }
                    booking.setValidated(true);
                    bookingRepository.save(booking);
                    return ResponseEntity.ok("ENTRADA VÁLIDA: Acceso permitido.");
                })
                .orElse(ResponseEntity.status(404).body("ERROR: Reserva no encontrada."));
    }
}
