package com.cineverse.api.config;

import com.cineverse.api.entities.Room;
import com.cineverse.api.repositories.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoomRepository roomRepository;

    public DataLoader(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo creamos las salas si la tabla está vacía
        if (roomRepository.count() == 0) {
            Room sala1 = new Room();
            sala1.setName("Sala 1 - IMAX");
            sala1.setCapacity(100);
            sala1.setRowsCount(10);
            sala1.setSeatsPerRow(10);

            Room sala2 = new Room();
            sala2.setName("Sala 2 - 3D Real");
            sala2.setCapacity(80);
            sala2.setRowsCount(8);
            sala2.setSeatsPerRow(10);

            Room sala3 = new Room();
            sala3.setName("Sala VIP - Luxury");
            sala3.setCapacity(30);
            sala3.setRowsCount(5);
            sala3.setSeatsPerRow(6);

            roomRepository.saveAll(List.of(sala1, sala2, sala3));
            System.out.println(">> Salas de cine inicializadas correctamente.");
        }
    }
}