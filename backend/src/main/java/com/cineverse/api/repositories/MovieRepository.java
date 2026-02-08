package com.cineverse.api.repositories;

import com.cineverse.api.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);
    // Busca películas cuyo título contenga la cadena enviada, ignorando mayúsculas
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
