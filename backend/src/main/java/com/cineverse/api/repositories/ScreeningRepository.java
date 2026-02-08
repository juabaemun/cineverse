package com.cineverse.api.repositories;

import com.cineverse.api.entities.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    List<Screening> findByMovieId(Long movieId);

    // Mantenemos este para comprobaciones rápidas de hora exacta
    @Query("SELECT COUNT(s) > 0 FROM Screening s WHERE s.room.id = :roomId AND s.startTime = :startTime")
    boolean existsByRoomAndStartTime(@Param("roomId") Long roomId, @Param("startTime") LocalDateTime startTime);


    @Query(value = "SELECT s.* FROM screenings s " +
            "JOIN movies m ON s.movie_id = m.id " +
            "WHERE s.room_id = :roomId " +
            "AND :newStart < DATE_ADD(s.start_time, INTERVAL m.duration MINUTE) " +
            "AND :newEnd > s.start_time",
            nativeQuery = true)
    List<Screening> findOverlappingScreenings(
            @Param("roomId") Long roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );
}


