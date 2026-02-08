package com.cineverse.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int capacity;
    private int rowsCount;
    private int seatsPerRow;

    @PrePersist
    @PreUpdate
    public void calculateCapacity() {
        this.capacity = this.rowsCount * this.seatsPerRow;
    }

    @OneToMany(mappedBy = "room")
    @JsonIgnore // <--- ESTO ES VITAL
    private List<Screening> screenings;
}


