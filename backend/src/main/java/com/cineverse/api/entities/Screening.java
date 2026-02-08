package com.cineverse.api.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "screenings")
@Data
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDateTime startTime;
    private double price;

    /**
     * Este método permite que el Frontend reciba "endTime" en el JSON automáticamente.
     * Al no tener un atributo 'private LocalDateTime endTime', Hibernate no se confunde.
     */
    public LocalDateTime getEndTime() {
        if (this.startTime != null && this.movie != null && this.movie.getDuration() != null) {
            return this.startTime.plusMinutes(this.movie.getDuration());
        }
        return null;
    }
}