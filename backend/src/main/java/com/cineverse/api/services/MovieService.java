package com.cineverse.api.services;

import com.cineverse.api.entities.Movie;
import com.cineverse.api.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }
}
