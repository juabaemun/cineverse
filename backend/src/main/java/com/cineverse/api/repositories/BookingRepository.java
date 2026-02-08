package com.cineverse.api.repositories;

import com.cineverse.api.entities.Booking;
import com.cineverse.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Usamos el guion bajo para referenciar al ID de la entidad Screening
    boolean existsByScreening_IdAndSeat(Long screeningId, String seat);

    List<Booking> findByScreening_Id(Long screeningId);

    List<Booking> findByUser(User user);
}