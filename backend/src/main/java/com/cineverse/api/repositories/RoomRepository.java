package com.cineverse.api.repositories;

import com.cineverse.api.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // Aquí ya existe save, findAll y deleteById por heredar de JpaRepository
}
