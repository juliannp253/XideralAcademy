package com.academymty.testing.exception;

public class CursoLlenoException extends RuntimeException {
    public CursoLlenoException(String mensaje) {
        super(mensaje);
    }
}
