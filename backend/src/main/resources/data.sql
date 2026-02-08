-- 1. Crear algunas Salas
INSERT INTO rooms (id, name, capacity) VALUES (1, 'Sala IMAX 1', 100);
INSERT INTO rooms (id, name, capacity) VALUES (2, 'Sala VIP 2', 40);

-- 2. Crear algunas Películas (por si no has importado de SWAPI aún)
INSERT INTO movies (id, title, director) VALUES (1, 'Star Wars: A New Hope', 'George Lucas');
INSERT INTO movies (id, title, director) VALUES (2, 'The Empire Strikes Back', 'Irvin Kershner');

-- 3. Crear Sesiones (Screenings) - Esto es lo que une Película y Sala
-- Formato de fecha: 'YYYY-MM-DD HH:MM:SS'
INSERT INTO screenings (id, movie_id, room_id, start_time, price)
VALUES (1, 1, 1, '2026-01-20 18:00:00', 9.50);

INSERT INTO screenings (id, movie_id, room_id, start_time, price)
-- 4. Crear un Usuario de prueba (Password: 1234 - Asegúrate de que coincida con tu lógica de encriptación)
-- Si usas BCrypt, el valor será diferente, pero para pruebas rápidas sin seguridad estricta:
-- Sustituye la línea del usuario por esta:
-- El password es: 1234
INSERT INTO users (id, email, password, role)
VALUES (1, 'staff@cine.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMnm.Sth.8nC', 'ADMIN');