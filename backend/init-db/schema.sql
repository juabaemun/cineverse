-- 1. Creación de la Base de Datos
CREATE DATABASE IF NOT EXISTS cineverse_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cineverse_db;

-- 2. Usuarios y Roles (Seguridad JWT) [cite: 52]
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL, -- Hash BCrypt [cite: 52]
                       email VARCHAR(100) UNIQUE NOT NULL,
                       role ENUM('CLIENTE', 'EMPLEADO', 'ADMIN') NOT NULL DEFAULT 'CLIENTE' -- [cite: 11]
);

-- 3. Gestión de Películas [cite: 8, 48]
CREATE TABLE movies (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(150) NOT NULL,
                        duration INT, -- en minutos
                        synopsis TEXT,
                        image_url VARCHAR(255),
                        is_special_event BOOLEAN DEFAULT FALSE -- Para integración SWAPI [cite: 27, 31]
);

-- 4. Infraestructura y Salas [cite: 8]
CREATE TABLE rooms (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       rows_count INT NOT NULL,
                       cols_count INT NOT NULL
);

-- 5. Sesiones y Cartelera [cite: 8, 15]
CREATE TABLE sessions (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          movie_id INT,
                          room_id INT,
                          start_time DATETIME NOT NULL,
                          price DECIMAL(5,2) NOT NULL,
                          FOREIGN KEY (movie_id) REFERENCES movies(id),
                          FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 6. Butacas y Entradas (App Móvil) [cite: 16, 17]
CREATE TABLE seats (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       room_id INT,
                       row_num INT NOT NULL,
                       col_num INT NOT NULL,
                       status ENUM('DISPONIBLE', 'RESERVADA', 'MANTENIMIENTO') DEFAULT 'DISPONIBLE',
                       FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE tickets (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT,
                         session_id INT,
                         seat_id INT,
                         purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         qr_code_data VARCHAR(255), -- Para persistencia en Android Room [cite: 17, 63]
                         FOREIGN KEY (user_id) REFERENCES users(id),
                         FOREIGN KEY (session_id) REFERENCES sessions(id),
                         FOREIGN KEY (seat_id) REFERENCES seats(id)
);

-- 7. Soporte e Incidencias (Chat Sockets) [cite: 10, 21, 69]
CREATE TABLE support_tickets (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 client_id INT,
                                 employee_id INT,
                                 subject VARCHAR(100),
                                 status ENUM('ABIERTA', 'PROGRESO', 'CERRADA') DEFAULT 'ABIERTA',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (client_id) REFERENCES users(id),
                                 FOREIGN KEY (employee_id) REFERENCES users(id)
);

CREATE TABLE chat_messages (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               ticket_id INT,
                               sender_id INT,
                               message TEXT NOT NULL,
                               sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (ticket_id) REFERENCES support_tickets(id),
                               FOREIGN KEY (sender_id) REFERENCES users(id)
);