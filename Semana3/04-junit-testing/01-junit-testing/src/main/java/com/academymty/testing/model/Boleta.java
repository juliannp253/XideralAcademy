package com.academymty.testing.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Boleta {

    public static final double NOTA_MINIMA_APROBATORIA = 70.0;
    private final Alumno alumno; // HAS-A
    private final Map<String, Double> calificaciones = new HashMap<>();

    public Boleta(Alumno alumno) {
        if (alumno == null) {
            throw new IllegalArgumentException("El alumno no puede ser nulo");
        }
        this.alumno = alumno;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void agregarCalificacion(String materia, double nota) {
        if (materia == null || materia.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la materia no puede estar vacío");
        }
        if (nota < 0.0 || nota > 100.0) {
            throw new IllegalArgumentException("La calificación debe estar entre 0.0 y 100.0. Recibido: " + nota);
        }
        calificaciones.put(materia.trim(), nota);
    }

    public double calcularPromedio() {
        if (calificaciones.isEmpty()) {
            throw new IllegalStateException("No hay calificaciones registradas para calcular el promedio");
        }
        double suma = 0.0;
        for (double nota : calificaciones.values()) {
            suma += nota;
        }
        return suma / calificaciones.size();
    }

    public boolean estaAprobado() {
        return calcularPromedio() >= NOTA_MINIMA_APROBATORIA;
    }


    public String obtenerEstadoMateria(String materia) {
        Double nota = calificaciones.get(materia);
        if (nota == null) {
            throw new IllegalArgumentException("La materia '" + materia + "' no está registrada en la boleta");
        }
        return nota >= NOTA_MINIMA_APROBATORIA ? "APROBADO" : "REPROBADO";
    }

    public Map<String, Double> getCalificaciones() {
        return Collections.unmodifiableMap(calificaciones);
    }

    public int getCantidadMaterias() {
        return calificaciones.size();
    }
}
