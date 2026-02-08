package com.cineverse.api.controllers;

import com.cineverse.api.entities.Movie;
import com.cineverse.api.repositories.MovieRepository;
import com.cineverse.api.services.MovieService;
import com.cineverse.api.services.SwapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired private MovieService movieService;
    @Autowired private SwapiService swapiService;
    @Autowired private MovieRepository movieRepository;

    @GetMapping
    public ResponseEntity<List<Movie>> getAll() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String title) {
        List<Movie> movies = movieService.searchMovies(title);
        return movies.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(movies);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        return ResponseEntity.ok(movieService.createMovie(movie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movieDetails) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setTitle(movieDetails.getTitle());
                    movie.setSynopsis(movieDetails.getSynopsis());
                    movie.setDuration(movieDetails.getDuration());
                    movie.setImageUrl(movieDetails.getImageUrl());
                    return ResponseEntity.ok(movieRepository.save(movie));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        try {
            movieRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La película tiene sesiones activas.");
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<String> importData() {
        swapiService.importStarWarsMovies();
        return ResponseEntity.ok("Importación terminada.");
    }
}