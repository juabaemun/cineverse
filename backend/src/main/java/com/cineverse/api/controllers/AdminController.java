package com.cineverse.api.controllers;

import com.cineverse.api.entities.Room;
import com.cineverse.api.entities.Screening;
import com.cineverse.api.repositories.RoomRepository;
import com.cineverse.api.repositories.ScreeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ScreeningRepository screeningRepository;

    // --- GESTIÓN DE SALAS ---
    @PostMapping("/rooms")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN', 'EMPLOYEE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }

    // --- GESTIÓN DE SESIONES (SCREENINGS) ---
    @PostMapping("/screenings")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Screening> createScreening(@RequestBody Screening screening) {
        return ResponseEntity.ok(screeningRepository.save(screening));
    }

    @DeleteMapping("/screenings/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}