package com.cineverse.api.controllers;

import com.cineverse.api.entities.Room;
import com.cineverse.api.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        return roomRepository.findById(id).map(room -> {
            room.setName(roomDetails.getName());
            room.setRowsCount(roomDetails.getRowsCount());
            room.setSeatsPerRow(roomDetails.getSeatsPerRow());
            // La capacidad se recalculará en el @PrePersist/@PreUpdate de tu Entidad
            return ResponseEntity.ok(roomRepository.save(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            roomRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Esto saltará si hay sesiones de cine vinculadas a esta sala
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar la sala: tiene sesiones o datos vinculados.");
        }
    }
}