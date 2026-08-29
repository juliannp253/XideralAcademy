package com.academymty.testing.model;

public record InscripcionResultado(
        boolean exitosa,
        String folio,
        String mensaje,
        Alumno alumno
) { }
