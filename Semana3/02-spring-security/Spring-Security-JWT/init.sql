CREATE DATABASE IF NOT EXISTS pokemon_db;
USE pokemon_db;

CREATE TABLE IF NOT EXISTS pokemon (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    level INT NOT NULL
);

INSERT INTO pokemon (name, type, level) VALUES
('Pikachu', 'Eléctrico', 25),
('Charizard', 'Fuego', 36),
('Bulbasaur', 'Planta', 15);