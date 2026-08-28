USE pokemon_db;

DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS members;

--
-- Tabla de usuarios.
-- pw es char(68) porque Spring guarda: {bcrypt} (8) + hash BCrypt (60) = 68
--
CREATE TABLE members (
    user_id varchar(50) NOT NULL,
    pw char(68) NOT NULL,
    active tinyint NOT NULL,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO members (user_id, pw, active) VALUES
('john',  '{bcrypt}$2y$10$q5C89SItU5ZKPZlTspXrZOOcm7njHEeRF7dys6b.Bgo7NhKWbMGfG', 1),
('mary',  '{bcrypt}$2y$10$y0UvRlnLKlOh7nBfH8sNvuXUIVhvwOMYaz1ysyJoPYvwY8tCg.K/i', 1),
('susan', '{bcrypt}$2y$10$6eOesXl7A1E7kaE7UYulPu4h5o5r6Yqd/F/dPFMWx2kDTZA64qU1W', 1);

--
-- Tabla de roles. Un usuario puede tener varios.
-- OJO: aqui se guarda "ROLE_EMPLOYEE" con el prefijo ROLE_,
-- pero en Java se escribe hasRole("EMPLOYEE") SIN el prefijo. Spring lo agrega solo.
--
CREATE TABLE roles (
    user_id varchar(50) NOT NULL,
    role varchar(50) NOT NULL,
    UNIQUE KEY authorities_idx_1 (user_id, role),
    CONSTRAINT authorities_ibfk_1
        FOREIGN KEY (user_id) REFERENCES members (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO roles (user_id, role) VALUES
('john',  'ROLE_EMPLOYEE'),
('mary',  'ROLE_EMPLOYEE'),
('mary',  'ROLE_MANAGER'),
('susan', 'ROLE_EMPLOYEE'),
('susan', 'ROLE_MANAGER'),
('susan', 'ROLE_ADMIN');

SELECT m.user_id, m.active, r.role FROM members m JOIN roles r ON m.user_id = r.user_id ORDER BY m.user_id, r.role;