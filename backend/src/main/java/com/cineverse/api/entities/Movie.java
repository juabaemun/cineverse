package com.cineverse.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "movies")
@Data
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    private String imageUrl;

    // Para diferenciar peliculas normales de eventos de Star Wars [cite: 27, 30]
    private boolean isSpecialEvent;

    @OneToMany(mappedBy = "movie")
    @JsonIgnore // <--- ESTO ES VITAL
    private List<Screening> screenings;
}
