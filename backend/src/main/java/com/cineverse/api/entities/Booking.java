package com.cineverse.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "password"}) // Evitamos cargar las reservas del usuario aquí
    private User user;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    @JsonIgnoreProperties({"bookings"}) // Evitamos bucle con screening
    private Screening screening;

    private String seat;
    private boolean validated = false;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();
}