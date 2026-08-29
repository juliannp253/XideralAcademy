package com.academymty.testing.repository;

import com.academymty.testing.model.Alumno;
import java.util.Optional;

public interface AlumnoRepository {
    Optional<Alumno> findById(int id);
    Optional<Alumno> findByMatricula(String matricula);
    Alumno save(Alumno alumno);
    boolean existsByCurp(String curp);
    long count();
}
