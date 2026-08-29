package com.academymty.testing.model;

import com.academymty.testing.exception.CursoCerradoException;
import com.academymty.testing.exception.CursoLlenoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curso {

    private final int id;
    private final String nombre;
    private final int cupoMaximo;
    private final List<Alumno> alumnos = new ArrayList<>();
    private boolean cerrado = false;

    public Curso(int id, String nombre, int cupoMaximo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del curso no puede estar vacío");
        }
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser mayor a cero");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.cupoMaximo = cupoMaximo;
    }

    public void inscribir(Alumno alumno) {
        if (alumno == null) {
            throw new IllegalArgumentException("El alumno a inscribir no puede ser nulo");
        }
        if (cerrado) {
            throw new CursoCerradoException("No se pueden inscribir alumnos: el curso '" + nombre + "' ya está cerrado");
        }
        if (alumnos.size() >= cupoMaximo) {
            throw new CursoLlenoException("No hay cupo disponible en el curso '" + nombre + "'. Capacidad: " + cupoMaximo);
        }
        alumnos.add(alumno);
    }

    public void cerrarInscripciones() {
        this.cerrado = true;
    }

    public String generarActaCalificaciones() {
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "ACTA-CURSO-" + id + "-" + nombre.toUpperCase() + " [Total Alumnos: " + alumnos.size() + "]";
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public int getCupoDisponible() {
        return cupoMaximo - alumnos.size();
    }

    public boolean isCerrado() {
        return cerrado;
    }

    public List<Alumno> getAlumnos() {
        return Collections.unmodifiableList(alumnos);
    }
}
