package com.logistic.fast_track.core.model;

import java.time.LocalDate;

public abstract class Envio implements iRastreable, Comparable<Envio>{
    protected String idRastreo;
    protected double peso;
    protected LocalDate fechaCreacion;
    protected Cliente remitente;
    protected Direccion destino;

    public abstract double calcularTiempoEstimado();

    @Override
    public int compareTo(Envio otroEnvio){
        return this.fechaCreacion.compareTo(otroEnvio.fechaCreacion);
    }

    // Getters
    public String getIdRastreo() {
        return idRastreo;
    }

    public double getPeso() {
        return peso;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public Cliente getRemitente() {
        return remitente;
    }

    public Direccion getDestino() {
        return destino;
    }
}
