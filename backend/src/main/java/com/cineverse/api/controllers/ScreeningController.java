package com.cineverse.api.controllers;

import com.cineverse.api.dto.ScreeningDTO;
import com.cineverse.api.entities.Screening;
import com.cineverse.api.entities.Movie;
import com.cineverse.api.entities.Room;
import com.cineverse.api.repositories.MovieRepository;
import com.cineverse.api.repositories.ScreeningRepository;
import com.cineverse.api.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    @Autowired private ScreeningRepository screeningRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private RoomRepository roomRepository;

    @GetMapping
    public List<Screening> getAll() {
        return screeningRepository.findAll();
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createScreening(@RequestBody ScreeningDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Película no encontrada"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        // 1. Calcular cuándo terminaría la nueva sesión
        LocalDateTime newStart = dto.getStartTime();
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDuration());

        // 2. Verificar solapamientos
        // Si es una EDICIÓN, debemos excluir la propia sesión actual de la búsqueda
        List<Screening> overlaps = screeningRepository.findOverlappingScreenings(
                dto.getRoomId(), newStart, newEnd);

        if (!overlaps.isEmpty()) {
            Screening conflict = overlaps.get(0);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conflicto: La sala está ocupada por '" + conflict.getMovie().getTitle() +
                            "' hasta las " + conflict.getEndTime().toLocalTime());
        }

        // 3. Si no hay conflicto, guardar
        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setRoom(room);
        screening.setStartTime(newStart);
        screening.setPrice(dto.getPrice());

        return ResponseEntity.ok(screeningRepository.save(screening));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateScreening(@PathVariable Long id, @RequestBody ScreeningDTO dto) {
        return screeningRepository.findById(id).map(screening -> {
            Movie movie = movieRepository.findById(dto.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Película no encontrada"));
            Room room = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

            screening.setMovie(movie);
            screening.setRoom(room);
            screening.setStartTime(dto.getStartTime());
            screening.setPrice(dto.getPrice());

            Screening updated = screeningRepository.save(screening);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteScreening(@PathVariable Long id) {
        try {
            if (!screeningRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("La sesión no existe.");
            }

            // Borramos la sesión
            screeningRepository.deleteById(id);

            // Es vital devolver un 200 OK para que el Frontend sepa que terminó
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            // Si hay un error (ej: hay tickets vendidos), lo capturamos
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: comprueba si existen tickets asociados.");
        }
    }
}